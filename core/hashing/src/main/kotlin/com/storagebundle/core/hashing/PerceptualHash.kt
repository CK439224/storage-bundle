package com.storagebundle.core.hashing

/**
 * A 64-bit perceptual image hash.
 *
 * Two images are *visually* similar when their hashes have a small [distanceTo]. Unlike a
 * cryptographic hash, a small change to the image produces a small change to the value —
 * which is the property that lets the app catch burst-shot near-duplicates rather than only
 * byte-identical copies (PLAN.md §5.2).
 *
 * This type is deliberately free of Android dependencies so it can be unit-tested on the JVM
 * in milliseconds (PLAN.md §4).
 *
 * @property bits the packed hash value.
 */
@JvmInline
value class PerceptualHash(val bits: Long) {

    /**
     * Hamming distance to [other] — the number of differing bits, from 0 (identical) to 64.
     *
     * Distances are compared against a user-controlled strictness threshold; the default of
     * 10 is deliberately conservative because a false positive here can cost a real photo.
     */
    fun distanceTo(other: PerceptualHash): Int = java.lang.Long.bitCount(bits xor other.bits)

    /** Returns the hash as a fixed-width hex string, for logging and test fixtures. */
    override fun toString(): String = "PerceptualHash(${bits.toULong().toString(HEX_RADIX).padStart(HEX_WIDTH, '0')})"

    /** Hash-wide constants and parsing helpers. */
    companion object {
        private const val HEX_RADIX = 16
        private const val HEX_WIDTH = 16

        /** The number of bits in a hash — also the maximum possible distance. */
        const val BIT_COUNT: Int = 64

        /** Parses a hash from the hex form produced by [toString]. */
        fun fromHex(hex: String): PerceptualHash = PerceptualHash(hex.toULong(HEX_RADIX).toLong())
    }
}

/**
 * Computes difference hashes (dHash) from pre-scaled grayscale images.
 *
 * dHash compares each pixel with its right-hand neighbour, so it encodes the image's gradient
 * structure. That makes it robust to the transformations that dominate a real photo library —
 * re-compression, resizing, and brightness shifts — while staying sensitive to genuine
 * differences in content.
 *
 * The caller supplies an already-downscaled grayscale buffer. Decoding is the expensive part
 * and belongs on the Android side, where images are decoded at a target sample size and never
 * as full-resolution bitmaps (PLAN.md §5.2).
 */
object DifferenceHasher {

    /** Width of the input buffer: one extra column supplies each row's final comparison. */
    const val SOURCE_WIDTH: Int = 9

    /** Height of the input buffer. */
    const val SOURCE_HEIGHT: Int = 8

    /** Required size of the buffer passed to [hash]. */
    const val REQUIRED_SIZE: Int = SOURCE_WIDTH * SOURCE_HEIGHT

    /**
     * Computes the dHash of a grayscale buffer.
     *
     * @param grayscale luminance values in row-major order, [SOURCE_WIDTH] × [SOURCE_HEIGHT].
     *   Values may use any consistent scale; only relative order matters.
     * @return the 64-bit hash.
     * @throws IllegalArgumentException if [grayscale] is not exactly [REQUIRED_SIZE] long.
     */
    fun hash(grayscale: IntArray): PerceptualHash {
        require(grayscale.size == REQUIRED_SIZE) {
            "Expected $REQUIRED_SIZE samples (${SOURCE_WIDTH}x$SOURCE_HEIGHT), got ${grayscale.size}"
        }

        var bits = 0L
        var bitIndex = 0

        for (row in 0 until SOURCE_HEIGHT) {
            val rowOffset = row * SOURCE_WIDTH
            for (column in 0 until SOURCE_WIDTH - 1) {
                val left = grayscale[rowOffset + column]
                val right = grayscale[rowOffset + column + 1]
                if (left > right) {
                    bits = bits or (1L shl bitIndex)
                }
                bitIndex++
            }
        }

        return PerceptualHash(bits)
    }
}
