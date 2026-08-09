package com.storagebundle.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.storagebundle.core.common.di.ApplicationScope
import com.storagebundle.core.common.di.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Singleton

/** Provides the preferences DataStore. */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val PREFERENCES_NAME = "user_preferences"

    /** Builds the singleton preferences store, backed by app-private storage. */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope + dispatcher,
        produceFile = { context.preferencesDataStoreFile(PREFERENCES_NAME) },
    )
}
