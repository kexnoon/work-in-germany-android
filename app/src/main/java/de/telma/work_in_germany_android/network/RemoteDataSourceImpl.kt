package de.telma.work_in_germany_android.network

import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats

class RemoteDataSourceImpl() : RemoteDataSource {

    @Throws(JobFetchingException::class)
    override suspend fun fetchJobs(): RemoteJobsResponse {
        TODO("Not yet implemented")
    }

    @Throws(StatsFetchingException::class)
    override suspend fun fetchStats(): RemoteStats {
        TODO("Not yet implemented")
    }
}