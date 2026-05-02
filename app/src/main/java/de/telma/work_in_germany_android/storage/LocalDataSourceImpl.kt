package de.telma.work_in_germany_android.storage

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSourceImpl() : LocalDataSource {
    override suspend fun getStats(): Stats = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override suspend fun getJobs(): List<Job> = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override suspend fun saveStats(stats: Stats) = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override suspend fun saveJobs(jobs: List<Job>) = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }
}