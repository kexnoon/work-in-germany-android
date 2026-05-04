package de.telma.work_in_germany_android.data

import app.cash.turbine.test
import de.telma.work_in_germany_android.job
import de.telma.work_in_germany_android.model.JobCategory
import de.telma.work_in_germany_android.network.fakes.FakeRemoteDataSource
import de.telma.work_in_germany_android.remoteJob
import de.telma.work_in_germany_android.remoteJobsResponse
import de.telma.work_in_germany_android.remoteStats
import de.telma.work_in_germany_android.stats
import de.telma.work_in_germany_android.storage.fakes.FakeLocalDataSource
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
}
