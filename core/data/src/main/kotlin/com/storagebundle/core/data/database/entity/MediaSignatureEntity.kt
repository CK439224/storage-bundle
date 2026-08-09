package com.storagebundle.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A cached perceptual signature for one image in MediaStore.
 *
 * This table is what makes rescans incremental (PLAN.md §5.2): a row is reused when the
 * image's [dateModified] and [sizeBytes] are unchanged, so a second scan hashes only new or
 * edited images. It is shared by both media features — Screenshot Sweeper uses it for visual
 * clustering, Duplicate Photo Auditor for its BK-tree index — which is why it lives in
 * `:core:data` rather than in either feature module.
 *
 * @property mediaStoreId the MediaStore `_ID`. Stable for the lifetime of the media entry.
 * @property dateModified MediaStore `DATE_MODIFIED`, in seconds. Part of the staleness check.
 * @property sizeBytes file size in bytes. Second half of the staleness check — together with
 *   [dateModified] this catches edits that preserve the timestamp.
 * @property differenceHash 64-bit dHash packed into a Long. Hamming distance against another
 *   row's value is the visual-similarity measure.
 * @property widthPx decoded image width, used as a burst-detection prior.
 * @property heightPx decoded image height, used as a burst-detection prior.
 * @property capturedAtMillis capture time where MediaStore exposes one, else null. Burst
 *   sequences share a near-identical capture time.
 * @property indexedAtMillis when this row was written, for cache eviction and diagnostics.
 */
@Entity(
    tableName = "media_signature",
    indices = [
        Index(value = ["differenceHash"]),
        Index(value = ["capturedAtMillis"]),
    ],
)
data class MediaSignatureEntity(
    @PrimaryKey
    val mediaStoreId: Long,
    val dateModified: Long,
    val sizeBytes: Long,
    val differenceHash: Long,
    val widthPx: Int,
    val heightPx: Int,
    val capturedAtMillis: Long?,
    val indexedAtMillis: Long,
)
