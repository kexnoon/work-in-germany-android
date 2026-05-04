package de.telma.work_in_germany_android.network.fakes

import de.telma.work_in_germany_android.network.RemoteDataSource
import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats
import de.telma.work_in_germany_android.remoteJobsResponse
import de.telma.work_in_germany_android.remoteStats

class FakeRemoteDataSource(
    private val stats: RemoteStats = remoteStats(),
    private val jobs: RemoteJobsResponse = remoteJobsResponse(),
    private val statsThrowable: Throwable? = null,
    private val jobsThrowable: Throwable? = null
) : RemoteDataSource {
    var fetchStatsCalls = 0
        private set

    var fetchJobsCalls = 0
        private set

    override suspend fun fetchJobs(): RemoteJobsResponse {
        fetchJobsCalls++
        jobsThrowable?.let { throw it }
        return jobs
    }

    override suspend fun fetchStats(): RemoteStats {
        fetchStatsCalls++
        statsThrowable?.let { throw it }
        return stats
    }
}