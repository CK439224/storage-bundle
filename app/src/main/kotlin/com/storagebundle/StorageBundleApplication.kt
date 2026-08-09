package com.storagebundle

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point and Hilt root.
 *
 * Supplies WorkManager's [Configuration] so workers can be constructor-injected. The default
 * WorkManager initialiser is removed in the manifest; implementing
 * [Configuration.Provider] makes initialisation on-demand instead, which keeps it off the
 * startup path.
 *
 * Every long-running job in this app — library scans, OCR indexing, drift checks — runs
 * through WorkManager so it survives process death and stays inside the Android 15 `dataSync`
 * foreground-service limit (PLAN.md §0).
 */
@HiltAndroidApp
class StorageBundleApplication : Application(), Configuration.Provider {

    /** Hilt-aware factory so workers can take injected constructor parameters. */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
