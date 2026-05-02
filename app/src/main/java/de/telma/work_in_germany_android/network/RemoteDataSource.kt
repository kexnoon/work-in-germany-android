package de.telma.work_in_germany_android.network

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats

interface RemoteDataSource {
    suspend fun fetchJobs(): List<Job>
    suspend fun fetchStats(): Stats
}