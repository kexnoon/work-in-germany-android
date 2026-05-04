package de.telma.work_in_germany_android.network

import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.path

class RemoteDataSourceImpl(
    private val client: HttpClient
) : RemoteDataSource {

    private companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/" +
                "ashishtiwari03/work-in-germany/refs/heads/main/data"
        const val JOBS_ENDPOINT = "jobs.json"
        const val STATS_ENDPOINT = "stats.json"
    }

    @Throws(JobFetchingException::class)
    override suspend fun fetchJobs(): RemoteJobsResponse {
        val response = try {
            client.get("$BASE_URL/$JOBS_ENDPOINT")
        } catch (e: Exception) {
            throw JobFetchingException("Failed to fetch jobs: ${e.message}", e)
        }
        when (response.status.value) {
            in 200..299 -> return response.body() as RemoteJobsResponse
            else -> throw JobFetchingException("Failed to fetch jobs: ${response.status}")
        }
    }

    @Throws(StatsFetchingException::class)
    override suspend fun fetchStats(): RemoteStats {
        val response = try {
            client.get("$BASE_URL/$STATS_ENDPOINT")
        } catch (e: Exception) {
            throw StatsFetchingException("Failed to fetch jobs: ${e.message}", e)
        }
        when (response.status.value) {
            in 200..299 -> return response.body() as RemoteStats
            else -> throw StatsFetchingException("Failed to fetch jobs: ${response.status}")
        }
    }
}