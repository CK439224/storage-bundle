package com.storagebundle.core.data.di

import android.content.Context
import androidx.room.Room
import com.storagebundle.core.data.database.StorageBundleDatabase
import com.storagebundle.core.data.database.dao.MediaSignatureDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the Room database and its DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Builds the singleton database instance. */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): StorageBundleDatabase = Room.databaseBuilder(
        context,
        StorageBundleDatabase::class.java,
        StorageBundleDatabase.DATABASE_NAME,
    ).build()

    /** Exposes the media-signature DAO for injection. */
    @Provides
    fun provideMediaSignatureDao(
        database: StorageBundleDatabase,
    ): MediaSignatureDao = database.mediaSignatureDao()
}
