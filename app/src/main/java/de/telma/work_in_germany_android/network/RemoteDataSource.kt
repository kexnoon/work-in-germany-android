package de.telma.work_in_germany_android.network

import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats
import kotlin.jvm.Throws

interface RemoteDataSource {
    @Throws(JobFetchingException::class)
    suspend fun fetchJobs(): RemoteJobsResponse

    @Throws(StatsFetchingException::class)
    suspend fun fetchStats(): RemoteStats
}

class JobFetchingException(message: String, cause: Throwable? = null) : Exception(message, cause)
class StatsFetchingException(message: String, cause: Throwable? = null) : Exception(message, cause)