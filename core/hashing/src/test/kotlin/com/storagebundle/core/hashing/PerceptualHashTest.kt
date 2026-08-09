package com.storagebundle.core.hashing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the perceptual hash.
 *
 * These run on the JVM with no emulator, which is the reason `:core:hashing` is kept free of
 * Android dependencies (PLAN.md §4). The full precision/recall corpus arrives with v0.2;
 * these cover the algebraic properties the clustering logic will rely on.
 */
class PerceptualHashTest {

    @Test
    fun `identical hashes have zero distance`() {
        val hash = PerceptualHash(0x0123_4567_89AB_CDEFuL.toLong())

        assertEquals(0, hash.distanceTo(hash))
    }

    @Test
    fun `distance counts differing bits`() {
        val a = PerceptualHash(0b0000L)
        val b = PerceptualHash(0b1011L)

        assertEquals(3, a.distanceTo(b))
    }

    @Test
    fun `distance is symmetric`() {
        val a = PerceptualHash(0x00FF_00FF_00FF_00FFuL.toLong())
        val b = PerceptualHash(0x0F0F_0F0F_0F0F_0F0FuL.toLong())

        assertEquals(a.distanceTo(b), b.distanceTo(a))
    }

    @Test
    fun `inverted hash is maximally distant`() {
        val a = PerceptualHash(0L)
        val b = PerceptualHash(-1L)

        assertEquals(PerceptualHash.BIT_COUNT, a.distanceTo(b))
    }

    @Test
    fun `hex round trip preserves value`() {
        val original = PerceptualHash(0xDEAD_BEEF_1234_5678uL.toLong())
        val hex = original.toString().substringAfter('(').substringBefore(')')

        assertEquals(original, PerceptualHash.fromHex(hex))
    }

    @Test
    fun `uniform image hashes to zero`() {
        val flat = IntArray(DifferenceHasher.REQUIRED_SIZE) { GRAY_MIDPOINT }

        assertEquals(PerceptualHash(0L), DifferenceHasher.hash(flat))
    }

    @Test
    fun `horizontal gradient produces a stable hash`() {
        // Each row increases left to right, so no pixel exceeds its right neighbour.
        val gradient = IntArray(DifferenceHasher.REQUIRED_SIZE) { index ->
            index % DifferenceHasher.SOURCE_WIDTH * GRADIENT_STEP
        }

        assertEquals(PerceptualHash(0L), DifferenceHasher.hash(gradient))
    }

    @Test
    fun `reversed gradient sets every bit`() {
        // Each row decreases left to right, so every pixel exceeds its right neighbour.
        val gradient = IntArray(DifferenceHasher.REQUIRED_SIZE) { index ->
            (DifferenceHasher.SOURCE_WIDTH - index % DifferenceHasher.SOURCE_WIDTH) * GRADIENT_STEP
        }

        assertEquals(PerceptualHash(-1L), DifferenceHasher.hash(gradient))
    }

    @Test
    fun `a small change produces a small distance`() {
        val base = IntArray(DifferenceHasher.REQUIRED_SIZE) { index ->
            index % DifferenceHasher.SOURCE_WIDTH * GRADIENT_STEP
        }
        val tweaked = base.copyOf().also { it[0] = Int.MAX_VALUE }

        val distance = DifferenceHasher.hash(base).distanceTo(DifferenceHasher.hash(tweaked))

        // This is the property the whole near-duplicate feature depends on: perceptually
        // small edits must not scatter the hash the way a cryptographic digest would.
        assertTrue("Expected a small distance but was $distance", distance in 1..2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `wrong buffer size is rejected`() {
        DifferenceHasher.hash(IntArray(DifferenceHasher.REQUIRED_SIZE - 1))
    }

    private companion object {
        const val GRAY_MIDPOINT = 128
        const val GRADIENT_STEP = 10
    }
}
