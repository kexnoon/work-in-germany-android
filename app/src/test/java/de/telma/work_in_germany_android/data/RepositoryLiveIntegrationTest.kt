package de.telma.work_in_germany_android.data

import app.cash.turbine.test
import de.telma.work_in_germany_android.network.RemoteDataSourceImpl
import de.telma.work_in_germany_android.storage.LocalDataSourceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class RepositoryLiveIntegrationTest {

    private lateinit var rootDir: File
    private lateinit var client: HttpClient

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Before
    fun setUp() {
        rootDir = createTempDirectory().toFile()
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
                json(json, ContentType.Text.Plain)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000L
            }
        }
    }

    @After
    fun tearDown() {
        if (::client.isInitialized) {
            client.close()
        }
        if (::rootDir.isInitialized) {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun `sync with real endpoints downloads saves and populates cache`() = runBlocking {
        val localDataSource = LocalDataSourceImpl(
            rootDir = rootDir,
            json = json
        )
        val remoteDataSource = RemoteDataSourceImpl(client)
        val repository = RepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource
        )

        repository.cache.test {
            assertEquals(null, awaitItem())

            val result = repository.sync()

            assertEquals(Repository.CacheUpdateResult.Updated, result)

            val cache = awaitItem()
            assertNotNull(cache)
            assertTrue(cache!!.jobs.all().isNotEmpty())
            assertTrue(cache.categories.isNotEmpty())
            assertTrue(cache.lastUpdated.isNotBlank())

            assertTrue(localDataSource.checkIfFilesExist())
            assertTrue(localDataSource.getJobs().isNotEmpty())
            assertTrue(localDataSource.getStats().lastUpdated.isNotBlank())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
