package com.storagebundle.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.storagebundle.core.data.database.dao.MediaSignatureDao
import com.storagebundle.core.data.database.entity.MediaSignatureEntity

/**
 * The app's single Room database.
 *
 * Stored in app-private storage and excluded from backup and device transfer
 * (`data_extraction_rules.xml`), because later versions add the OCR index here — text lifted
 * off the user's screen, and the most sensitive thing the app holds (PLAN.md §6).
 *
 * Schemas are exported to `core/data/schemas/` and committed, so migrations can be reviewed
 * as a diff and tested against real historical schemas.
 */
@Database(
    entities = [MediaSignatureEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class StorageBundleDatabase : RoomDatabase() {

    /** Cached perceptual signatures, shared by both media features. */
    abstract fun mediaSignatureDao(): MediaSignatureDao

    /** Database file name and other shared constants. */
    companion object {
        /** On-disk name of the database file. */
        const val DATABASE_NAME: String = "storage_bundle.db"
    }
}
