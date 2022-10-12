/*
 * Copyright (c) 2022 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.mobile.android.vpn.health

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.duckduckgo.anvil.annotations.ContributesWorker
import com.duckduckgo.app.global.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.pixels.DeviceShieldPixels
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ContributesWorker(AppScope::class)
class CPUMonitorWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    @Inject
    lateinit var deviceShieldPixels: DeviceShieldPixels

    @Inject
    lateinit var cpuUsageReader: CPUUsageReader

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    // TODO: move thresholds to remote config
    private val alertThresholds = listOf(30, 20, 10, 5).sortedDescending()

    override suspend fun doWork(): Result {
        return withContext(dispatcherProvider.io()) {
            try {
                print("WORKER STARTED")
                val avgCPUUsagePercent = cpuUsageReader.readCPUUsage()
                alertThresholds.forEach {
                    if (avgCPUUsagePercent > it) {
                        deviceShieldPixels.sendCPUUsageAlert(it)
                        return@withContext Result.success()
                    }
                }
            } catch (e: Exception) {
                Timber.e("Could not read CPU usage", e)
                return@withContext Result.failure()
            }

            return@withContext Result.success()
        }
    }
}
