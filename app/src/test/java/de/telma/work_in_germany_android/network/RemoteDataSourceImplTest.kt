package de.telma.work_in_germany_android.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteDataSourceImplTest {

    @Test
    fun `fetchStats requests stats endpoint and returns decoded RemoteStats`() = runBlocking {
        var requestedUrl = ""
        val client = testClient { request ->
            requestedUrl = request.url.toString()
            respondJson(STATS_JSON)
        }
        val dataSource = RemoteDataSourceImpl(client)

        val stats = dataSource.fetchStats()

        assertEquals(true, requestedUrl.endsWith("/stats.json"))
        assertEquals(33354, stats.totalJobs)
        assertEquals(4420, stats.visaFriendly)
        assertEquals(449, stats.englishFriendly)
        assertEquals(8775, stats.companiesTracked)
        assertEquals(3338, stats.categories["Software Engineering"])
        assertEquals(9181, stats.categories["Other"])
        assertEquals("2026-05-02 18:59 UTC", stats.lastUpdated)
    }

    @Test
    fun `fetchJobs requests jobs endpoint and returns decoded RemoteJobsResponse`() = runBlocking {
        var requestedUrl = ""
        val client = testClient { request ->
            requestedUrl = request.url.toString()
            respondJson(JOBS_JSON)
        }
        val dataSource = RemoteDataSourceImpl(client)

        val response = dataSource.fetchJobs()
        val job = response.jobs.single()

        assertEquals(true, requestedUrl.endsWith("/jobs.json"))
        assertEquals(1, response.metadata.total)
        assertEquals("2026-05-02 18:59 UTC", response.metadata.lastUpdated)
        assertEquals("job-1", job.id)
        assertEquals("Example GmbH", job.company)
        assertEquals("product", job.companyType)
        assertEquals("Android Developer", job.title)
        assertEquals("Berlin", job.location)
        assertEquals("https://example.com/job-1", job.url)
        assertEquals("2026-05-02", job.postedAt)
        assertEquals("Software Engineering", job.category)
        assertEquals("english", job.language)
        assertEquals("yes", job.visa)
        assertEquals("source", job.source)
        assertEquals("2026-05-02T18:59:00+00:00", job.lastSeen)
        assertEquals("2026-05-01T18:59:00+00:00", job.firstSeen)
    }

    @Test(expected = StatsFetchingException::class)
    fun `fetchStats throws StatsFetchingException on non 2xx response`() = runBlocking {
        val dataSource = RemoteDataSourceImpl(
            testClient {
                respond(
                    content = "",
                    status = HttpStatusCode.InternalServerError
                )
            }
        )

        dataSource.fetchStats()
        Unit
    }

    @Test(expected = JobFetchingException::class)
    fun `fetchJobs throws JobFetchingException on non 2xx response`() = runBlocking {
        val dataSource = RemoteDataSourceImpl(
            testClient {
                respond(
                    content = "",
                    status = HttpStatusCode.InternalServerError
                )
            }
        )

        dataSource.fetchJobs()
        Unit
    }

    private fun testClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    private fun MockRequestHandleScope.respondJson(content: String): HttpResponseData {
        return respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }

    private companion object {
        const val STATS_JSON = """
            {
              "total_jobs": 33354,
              "visa_friendly": 4420,
              "english_friendly": 449,
              "companies_tracked": 8775,
              "categories": {
                "Software Engineering": 3338,
                "Other": 9181
              },
              "last_updated": "2026-05-02 18:59 UTC"
            }
        """

        const val JOBS_JSON = """
            {
              "metadata": {
                "total": 1,
                "last_updated": "2026-05-02 18:59 UTC"
              },
              "jobs": [
                {
                  "id": "job-1",
                  "company": "Example GmbH",
                  "company_type": "product",
                  "title": "Android Developer",
                  "location": "Berlin",
                  "url": "https://example.com/job-1",
                  "posted_at": "2026-05-02",
                  "category": "Software Engineering",
                  "language": "english",
                  "visa": "yes",
                  "source": "source",
                  "last_seen": "2026-05-02T18:59:00+00:00",
                  "first_seen": "2026-05-01T18:59:00+00:00"
                }
              ]
            }
        """
    }
}
