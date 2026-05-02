package de.telma.work_in_germany_android.network

import androidx.annotation.Discouraged
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDataSourceImpl() : RemoteDataSource {

    override suspend fun fetchJobs(): List<Job> = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchStats(): Stats = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }
}