package de.telma.work_in_germany_android.storage

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats

interface LocalDataSource {

    suspend fun getStats(): Stats
    suspend fun getJobs(): List<Job>

    suspend fun saveStats(stats: Stats)
    suspend fun saveJobs(jobs: List<Job>)

}