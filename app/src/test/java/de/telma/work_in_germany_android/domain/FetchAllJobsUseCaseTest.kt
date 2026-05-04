package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.model.CachedJobs
import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchAllJobsUseCaseTest {

    @Test
    fun `sync updated with cache returns success`() = runBlocking {
        val category = JobCategory("Software Engineering", 1)
        val jobs = listOf(job(id = "job-1", category = category))
        val repository = FakeRepository(
            initialSnapshot = snapshot(
                jobs = jobs,
                categories = listOf(category)
            ),
            syncResult = Repository.CacheUpdateResult.Updated
        )
        val useCase = FetchAllJobsUseCase(repository)

        val result = useCase()

        assertEquals(
            FetchAllJobsUseCase.Result.Success(
                jobs = jobs,
                categories = listOf(category)
            ),
            result
        )
    }

    @Test
    fun `sync updated with empty cache returns error`() = runBlocking {
        val repository = FakeRepository(
            initialSnapshot = null,
            syncResult = Repository.CacheUpdateResult.Updated
        )
        val useCase = FetchAllJobsUseCase(repository)

        val result = useCase()

        assertTrue(result is FetchAllJobsUseCase.Result.Error)
        assertEquals(
            "Cache is empty after update",
            (result as FetchAllJobsUseCase.Result.Error).t.message
        )
    }

    @Test
    fun `sync unchanged returns no updates`() = runBlocking {
        val repository = FakeRepository(
            syncResult = Repository.CacheUpdateResult.Unchanged
        )
        val useCase = FetchAllJobsUseCase(repository)

        val result = useCase()

        assertEquals(FetchAllJobsUseCase.Result.NoUpdates, result)
    }

    @Test
    fun `sync error returns error`() = runBlocking {
        val throwable = RuntimeException("Sync failed")
        val repository = FakeRepository(
            syncResult = Repository.CacheUpdateResult.Error(throwable)
        )
        val useCase = FetchAllJobsUseCase(repository)

        val result = useCase()

        assertEquals(FetchAllJobsUseCase.Result.Error(throwable), result)
    }

    private class FakeRepository(
        initialSnapshot: JobCacheSnapshot? = null,
        private val syncResult: Repository.CacheUpdateResult = Repository.CacheUpdateResult.Updated
    ) : Repository {
        private val cacheFlow = MutableStateFlow(initialSnapshot)

        override val cache: StateFlow<JobCacheSnapshot?> = cacheFlow

        override suspend fun sync(): Repository.CacheUpdateResult = syncResult
    }

    private companion object {
        fun snapshot(
            jobs: List<Job>,
            categories: List<JobCategory>
        ): JobCacheSnapshot {
            return JobCacheSnapshot(
                jobs = CachedJobs.from(jobs),
                categories = categories,
                lastUpdated = "2026-05-04 12:00 UTC"
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
