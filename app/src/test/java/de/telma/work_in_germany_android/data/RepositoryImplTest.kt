package de.telma.work_in_germany_android.data

import app.cash.turbine.test
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory
import de.telma.work_in_germany_android.model.Stats
import de.telma.work_in_germany_android.network.RemoteDataSource
import de.telma.work_in_germany_android.network.model.RemoteJob
import de.telma.work_in_germany_android.network.model.RemoteJobsMetadata
import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats
import de.telma.work_in_germany_android.storage.LocalDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryImplTest {

    @Test
    fun `first sync without local files fetches remote data saves it and updates cache`() = runBlocking {
        val localDataSource = FakeLocalDataSource(filesExist = false)
        val remoteDataSource = FakeRemoteDataSource(
            stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
            jobs = remoteJobsResponse(
                remoteJob(
                    id = "job-1",
                    category = "Software Engineering",
                    postedAt = "2026-05-03"
                )
            )
        )
        val repository = RepositoryImpl(localDataSource, remoteDataSource)

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertEquals(Repository.CacheUpdateResult.Updated, result)
            assertEquals(1, remoteDataSource.fetchStatsCalls)
            assertEquals(1, remoteDataSource.fetchJobsCalls)
            assertEquals(1, localDataSource.saveJobsCalls)
            assertEquals(1, localDataSource.saveStatsCalls)

            val cache = awaitItem()
            assertNotNull(cache)
            assertEquals("2026-05-03 12:00 UTC", cache?.lastUpdated)
            assertEquals(listOf("job-1"), cache?.jobs?.all()?.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote source updated saves remote data and updates cache`() = runBlocking {
        val localCategory = JobCategory(name = "Old Category", amount = 1)
        val localDataSource = FakeLocalDataSource(
            stats = stats(
                lastUpdated = "2026-05-03 06:00 UTC",
                categories = listOf(localCategory)
            ),
            jobs = listOf(job(id = "old-job", category = localCategory))
        )
        val remoteDataSource = FakeRemoteDataSource(
            stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
            jobs = remoteJobsResponse(
                remoteJob(
                    id = "new-job",
                    category = "Software Engineering",
                    postedAt = "2026-05-03"
                )
            )
        )
        val repository = RepositoryImpl(localDataSource, remoteDataSource)

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertEquals(Repository.CacheUpdateResult.Updated, result)
            assertEquals(1, remoteDataSource.fetchJobsCalls)
            assertEquals(1, localDataSource.saveJobsCalls)
            assertEquals(1, localDataSource.saveStatsCalls)

            val cache = awaitItem()
            assertNotNull(cache)
            assertEquals("2026-05-03 12:00 UTC", cache?.lastUpdated)
            assertEquals(listOf("Software Engineering"), cache?.categories?.map { it.name })
            assertEquals(listOf("new-job"), cache?.jobs?.all()?.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote source has not updated returns unchanged without updating cache`() = runBlocking {
        val category = JobCategory(name = "Software Engineering", amount = 2)
        val localDataSource = FakeLocalDataSource(
            stats = stats(
                lastUpdated = "2026-05-03 12:00 UTC",
                categories = listOf(category)
            ),
            jobs = listOf(job(id = "local-job", category = category))
        )
        val remoteDataSource = FakeRemoteDataSource(
            stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
            jobs = remoteJobsResponse()
        )
        val repository = RepositoryImpl(localDataSource, remoteDataSource)

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertEquals(Repository.CacheUpdateResult.Unchanged, result)
            assertEquals(0, remoteDataSource.fetchJobsCalls)
            assertEquals(0, localDataSource.saveJobsCalls)
            assertEquals(0, localDataSource.saveStatsCalls)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote stats fetch fails with local files returns error without updating cache`() = runBlocking {
        val category = JobCategory(name = "Software Engineering", amount = 1)
        val statsFailure = RuntimeException("Stats fetch failed")
        val localDataSource = FakeLocalDataSource(
            stats = stats(
                lastUpdated = "2026-05-03 06:00 UTC",
                categories = listOf(category)
            ),
            jobs = listOf(job(id = "local-job", category = category))
        )
        val remoteDataSource = FakeRemoteDataSource(
            statsThrowable = statsFailure,
            jobs = remoteJobsResponse()
        )
        val repository = RepositoryImpl(localDataSource, remoteDataSource)

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertTrue(result is Repository.CacheUpdateResult.Error)
            assertEquals(statsFailure, (result as Repository.CacheUpdateResult.Error).t)
            assertEquals(0, remoteDataSource.fetchJobsCalls)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote stats fetch fails without local files returns error without updating cache`() = runBlocking {
        val statsFailure = RuntimeException("Stats fetch failed")
        val repository = RepositoryImpl(
            localDataSource = FakeLocalDataSource(filesExist = false),
            remoteDataSource = FakeRemoteDataSource(
                statsThrowable = statsFailure,
                jobs = remoteJobsResponse()
            )
        )

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertTrue(result is Repository.CacheUpdateResult.Error)
            assertEquals(statsFailure, (result as Repository.CacheUpdateResult.Error).t)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote jobs fetch fails after remote update returns error without saving or updating cache`() = runBlocking {
        val category = JobCategory(name = "Software Engineering", amount = 1)
        val jobsFailure = RuntimeException("Jobs fetch failed")
        val localDataSource = FakeLocalDataSource(
            stats = stats(
                lastUpdated = "2026-05-03 06:00 UTC",
                categories = listOf(category)
            ),
            jobs = listOf(job(id = "local-job", category = category))
        )
        val remoteDataSource = FakeRemoteDataSource(
            stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
            jobsThrowable = jobsFailure
        )
        val repository = RepositoryImpl(localDataSource, remoteDataSource)

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertTrue(result is Repository.CacheUpdateResult.Error)
            assertEquals(jobsFailure, (result as Repository.CacheUpdateResult.Error).t)
            assertEquals(0, localDataSource.saveJobsCalls)
            assertEquals(0, localDataSource.saveStatsCalls)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote jobs fetch fails without local files returns error without updating cache`() = runBlocking {
        val jobsFailure = RuntimeException("Jobs fetch failed")
        val repository = RepositoryImpl(
            localDataSource = FakeLocalDataSource(filesExist = false),
            remoteDataSource = FakeRemoteDataSource(
                stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
                jobsThrowable = jobsFailure
            )
        )

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertTrue(result is Repository.CacheUpdateResult.Error)
            assertEquals(jobsFailure, (result as Repository.CacheUpdateResult.Error).t)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving jobs fails returns error without saving stats or updating cache`() = runBlocking {
        val storageFailure = RuntimeException("Jobs save failed")
        val localDataSource = FakeLocalDataSource(
            filesExist = false,
            saveJobsThrowable = storageFailure
        )
        val repository = RepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = FakeRemoteDataSource(
                stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
                jobs = remoteJobsResponse(remoteJob(id = "remote-job"))
            )
        )

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertTrue(result is Repository.CacheUpdateResult.Error)
            assertEquals(storageFailure, (result as Repository.CacheUpdateResult.Error).t)
            assertEquals(1, localDataSource.saveJobsCalls)
            assertEquals(0, localDataSource.saveStatsCalls)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving stats fails returns error without updating cache`() = runBlocking {
        val storageFailure = RuntimeException("Stats save failed")
        val localDataSource = FakeLocalDataSource(
            filesExist = false,
            saveStatsThrowable = storageFailure
        )
        val repository = RepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = FakeRemoteDataSource(
                stats = remoteStats(lastUpdated = "2026-05-03 12:00 UTC"),
                jobs = remoteJobsResponse(remoteJob(id = "remote-job"))
            )
        )

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertTrue(result is Repository.CacheUpdateResult.Error)
            assertEquals(storageFailure, (result as Repository.CacheUpdateResult.Error).t)
            assertEquals(1, localDataSource.saveJobsCalls)
            assertEquals(1, localDataSource.saveStatsCalls)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `converted cache content uses stats categories and last updated`() = runBlocking {
        val repository = RepositoryImpl(
            localDataSource = FakeLocalDataSource(filesExist = false),
            remoteDataSource = FakeRemoteDataSource(
                stats = remoteStats(
                    lastUpdated = "2026-05-03 12:00 UTC",
                    categories = mapOf("Software Engineering" to 3338)
                ),
                jobs = remoteJobsResponse(
                    remoteJob(
                        id = "job-1",
                        category = "Software Engineering"
                    )
                )
            )
        )

        repository.cache.test {
            assertNull(awaitItem())

            val result = repository.sync()

            assertEquals(Repository.CacheUpdateResult.Updated, result)

            val cache = awaitItem()
            val job = cache?.jobs?.all()?.single()
            assertEquals("2026-05-03 12:00 UTC", cache?.lastUpdated)
            assertEquals(listOf(JobCategory("Software Engineering", 3338)), cache?.categories)
            assertEquals(JobCategory("Software Engineering", 3338), job?.category)
            assertEquals(3338, job?.category?.amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeLocalDataSource(
        private var stats: Stats = stats(),
        private var jobs: List<Job> = emptyList(),
        private val filesExist: Boolean = true,
        private val saveJobsThrowable: Throwable? = null,
        private val saveStatsThrowable: Throwable? = null
    ) : LocalDataSource {
        var saveJobsCalls = 0
            private set

        var saveStatsCalls = 0
            private set

        override suspend fun checkIfFilesExist(): Boolean = filesExist

        override suspend fun getStats(): Stats = stats

        override suspend fun getJobs(): List<Job> = jobs

        override suspend fun saveJobs(jobs: List<Job>) {
            saveJobsCalls++
            saveJobsThrowable?.let { throw it }
            this.jobs = jobs
        }

        override suspend fun saveStats(stats: Stats) {
            saveStatsCalls++
            saveStatsThrowable?.let { throw it }
            this.stats = stats
        }
    }

    private class FakeRemoteDataSource(
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

    private companion object {
        fun stats(
            lastUpdated: String = "2026-05-03 12:00 UTC",
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

        fun remoteStats(
            lastUpdated: String = "2026-05-03 12:00 UTC",
            categories: Map<String, Int> = mapOf("Software Engineering" to 1)
        ): RemoteStats {
            return RemoteStats(
                totalJobs = categories.values.sum(),
                visaFriendly = 0,
                englishFriendly = 0,
                companiesTracked = 0,
                categories = categories,
                lastUpdated = lastUpdated
            )
        }

        fun job(
            id: String,
            category: JobCategory = JobCategory("Software Engineering", 1),
            postedAt: String = "2026-05-03"
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
                lastSeen = "2026-05-03T12:00:00+00:00",
                firstSeen = "2026-05-03T12:00:00+00:00"
            )
        }

        fun remoteJobsResponse(vararg jobs: RemoteJob): RemoteJobsResponse {
            return RemoteJobsResponse(
                metadata = RemoteJobsMetadata(
                    total = jobs.size,
                    lastUpdated = "2026-05-03 12:00 UTC"
                ),
                jobs = jobs.toList()
            )
        }

        fun remoteJob(
            id: String,
            category: String = "Software Engineering",
            postedAt: String = "2026-05-03"
        ): RemoteJob {
            return RemoteJob(
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
                lastSeen = "2026-05-03T12:00:00+00:00",
                firstSeen = "2026-05-03T12:00:00+00:00"
            )
        }
    }
}
