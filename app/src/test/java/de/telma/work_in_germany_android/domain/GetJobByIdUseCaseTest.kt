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

class GetJobByIdUseCaseTest {

    @Test
    fun `cache has job with id returns success`() = runBlocking {
        val category = JobCategory("Software Engineering", 1)
        val expectedJob = job(id = "job-1", category = category)
        val repository = FakeRepository(
            initialSnapshot = snapshot(
                jobs = listOf(expectedJob),
                categories = listOf(category)
            )
        )
        val useCase = GetJobByIdUseCase(repository)

        val result = useCase.invoke("job-1")

        assertEquals(GetJobByIdUseCase.Result.Success(expectedJob), result)
    }

    @Test
    fun `cache does not have job id returns error`() = runBlocking {
        val category = JobCategory("Software Engineering", 1)
        val repository = FakeRepository(
            initialSnapshot = snapshot(
                jobs = listOf(job(id = "job-1", category = category)),
                categories = listOf(category)
            )
        )
        val useCase = GetJobByIdUseCase(repository)

        val result = useCase.invoke("missing-job")

        assertTrue(result is GetJobByIdUseCase.Result.Error)
        assertEquals(
            "Job with id missing-job not found",
            (result as GetJobByIdUseCase.Result.Error).t.message
        )
    }

    @Test
    fun `empty cache returns error`() = runBlocking {
        val repository = FakeRepository(initialSnapshot = null)
        val useCase = GetJobByIdUseCase(repository)

        val result = useCase.invoke("job-1")

        assertTrue(result is GetJobByIdUseCase.Result.Error)
        assertEquals(
            "Cache is empty",
            (result as GetJobByIdUseCase.Result.Error).t.message
        )
    }

    private class FakeRepository(
        initialSnapshot: JobCacheSnapshot? = null
    ) : Repository {
        private val cacheFlow = MutableStateFlow(initialSnapshot)

        override val cache: StateFlow<JobCacheSnapshot?> = cacheFlow

        override suspend fun sync(): Repository.CacheUpdateResult {
            return Repository.CacheUpdateResult.Unchanged
        }
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
