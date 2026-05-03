package de.telma.work_in_germany_android.storage

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory
import de.telma.work_in_germany_android.model.Stats
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class LocalDataSourceImplTest {

    private lateinit var rootDir: File
    private lateinit var dataSource: LocalDataSourceImpl

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Before
    fun setUp() {
        rootDir = createTempDirectory().toFile()
        dataSource = LocalDataSourceImpl(
            rootDir = rootDir,
            json = json
        )
    }

    @After
    fun tearDown() {
        rootDir.deleteRecursively()
    }

    @Test
    fun `checkIfFilesExist returns false when files are missing`() = runBlocking {
        assertFalse(dataSource.checkIfFilesExist())

        File(rootDir, "stats.json").writeText("{}")

        assertFalse(dataSource.checkIfFilesExist())

        File(rootDir, "stats.json").delete()
        File(rootDir, "jobs.json").writeText("{}")

        assertFalse(dataSource.checkIfFilesExist())
    }

    @Test
    fun `checkIfFilesExist returns true when both files exist`() = runBlocking {
        File(rootDir, "stats.json").writeText("{}")
        File(rootDir, "jobs.json").writeText("{}")

        assertTrue(dataSource.checkIfFilesExist())
    }

    @Test
    fun `saveStats then getStats returns saved stats`() = runBlocking {
        val stats = stats(
            lastUpdated = "2026-05-04 12:00 UTC",
            categories = listOf(JobCategory("Software Engineering", 10))
        )

        dataSource.saveStats(stats)

        assertEquals(stats, dataSource.getStats())
    }

    @Test
    fun `saveJobs then getJobs returns saved jobs`() = runBlocking {
        val jobs = listOf(
            job(
                id = "job-1",
                category = JobCategory("Software Engineering", 10)
            ),
            job(
                id = "job-2",
                category = JobCategory("Data Science", 5)
            )
        )

        dataSource.saveJobs(jobs)

        assertEquals(jobs, dataSource.getJobs())
    }

    @Test(expected = StatsRetrievalException::class)
    fun `getStats throws StatsRetrievalException when file is missing`() = runBlocking {
        dataSource.getStats()
        Unit
    }

    @Test(expected = JobsRetrievalException::class)
    fun `getJobs throws JobsRetrievalException when file is missing`() = runBlocking {
        dataSource.getJobs()
        Unit
    }

    @Test(expected = StatsRetrievalException::class)
    fun `getStats throws StatsRetrievalException when json is invalid`() = runBlocking {
        File(rootDir, "stats.json").writeText("{")

        dataSource.getStats()
        Unit
    }

    @Test(expected = JobsRetrievalException::class)
    fun `getJobs throws JobsRetrievalException when json is invalid`() = runBlocking {
        File(rootDir, "jobs.json").writeText("{")

        dataSource.getJobs()
        Unit
    }

    private companion object {
        fun stats(
            lastUpdated: String = "2026-05-04 12:00 UTC",
            categories: List<JobCategory> = listOf(JobCategory("Software Engineering", 1))
        ): Stats {
            return Stats(
                totalJobs = categories.sumOf { it.amount },
                visaFriendly = 0,
                englishFriendly = 0,
                companiesTracked = 0,
                categories = categories,
                lastUpdated = lastUpdated
            )
        }

        fun job(
            id: String,
            category: JobCategory,
            postedAt: String = "2026-05-04"
        ): Job {
            return Job(
                id = id,
                company = "Company $id",
                companyType = "product",
                title = "Android Developer",
                location = "Berlin",
                url = "https://example.com/$id",
                postedAt = postedAt,
                category = category,
                language = "english",
                visa = "yes",
                source = "source",
                lastSeen = "2026-05-04T12:00:00+00:00",
                firstSeen = "2026-05-04T12:00:00+00:00"
            )
        }
    }
}
