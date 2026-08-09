package com.storagebundle.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.storagebundle.core.data.database.entity.MediaSignatureEntity
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes cached perceptual signatures.
 *
 * All queries are Room-parameterised — no string concatenation anywhere in this app's SQL
 * (PLAN.md §6, Database Security).
 */
@Dao
interface MediaSignatureDao {

    /** Emits the number of cached signatures, for progress and diagnostics UI. */
    @Query("SELECT COUNT(*) FROM media_signature")
    fun observeCount(): Flow<Int>

    /** Returns the signature for [mediaStoreId], or null when it has not been indexed. */
    @Query("SELECT * FROM media_signature WHERE mediaStoreId = :mediaStoreId")
    suspend fun findById(mediaStoreId: Long): MediaSignatureEntity?

    /**
     * Returns every cached signature.
     *
     * Callers stream this into the BK-tree at scan start; it is not exposed to the UI.
     */
    @Query("SELECT * FROM media_signature")
    suspend fun getAll(): List<MediaSignatureEntity>

    /**
     * Returns the ids that are already indexed *and* unchanged, given the current MediaStore
     * state. Anything absent from the result needs rehashing.
     */
    @Query(
        """
        SELECT mediaStoreId FROM media_signature
        WHERE mediaStoreId IN (:mediaStoreIds)
        """,
    )
    suspend fun findIndexedIds(mediaStoreIds: List<Long>): List<Long>

    /** Inserts or replaces [signatures]. */
    @Upsert
    suspend fun upsertAll(signatures: List<MediaSignatureEntity>)

    /** Removes signatures whose media no longer exists. */
    @Query("DELETE FROM media_signature WHERE mediaStoreId NOT IN (:liveMediaStoreIds)")
    suspend fun deleteOrphans(liveMediaStoreIds: List<Long>)

    /** Clears the whole cache. Exposed to the user in settings. */
    @Query("DELETE FROM media_signature")
    suspend fun clear()
}
