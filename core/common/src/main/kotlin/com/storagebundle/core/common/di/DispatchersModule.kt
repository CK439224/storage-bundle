package com.storagebundle.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/** Marks the dispatcher used for disk, MediaStore, and database work. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** Marks the dispatcher used for CPU-bound work such as hashing and clustering. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

/** Marks the main/UI dispatcher. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher

/** Marks the application-lifetime coroutine scope. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

/**
 * Supplies coroutine dispatchers through injection rather than referencing [Dispatchers]
 * directly, so tests can substitute a deterministic scheduler.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    /** Dispatcher for blocking I/O: MediaStore queries, file reads, Room. */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /** Dispatcher for CPU-bound work: perceptual hashing, clustering, OCR post-processing. */
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /** Main-thread dispatcher. */
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    /**
     * A scope that outlives any single screen.
     *
     * Uses [SupervisorJob] so one failed background task cannot cancel the others — a scan
     * failing must not take the OCR indexer down with it.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
