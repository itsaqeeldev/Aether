package com.devsphere.aether.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Scheduler for background weather refresh
 * Sets up periodic work to keep cache fresh
 */
object WeatherRefreshScheduler {

    /**
     * Schedule periodic weather refresh
     * Runs every 4 hours when:
     * - Device has network connection
     * - Battery is not low
     */
    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(
            repeatInterval = 4, // Run every 4 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1, // Flexible window of 1 hour
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeatherRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            workRequest
        )
    }

    /**
     * Cancel scheduled weather refresh
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WeatherRefreshWorker.WORK_NAME)
    }

    /**
     * Check if work is scheduled
     */
    fun isScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WeatherRefreshWorker.WORK_NAME)
            .get()

        return workInfos.any { !it.state.isFinished }
    }
}