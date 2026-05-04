package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.model.CachedJobs
import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import de.telma.work_in_germany_android.job
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory
import de.telma.work_in_germany_android.snapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAllJobsInCategoryTest {

    @Test
    fun `cache has category returns success with jobs`() = runBlocking {
        val software = JobCategory("Software Engineering", 1)
        val data = JobCategory("Data Science", 1)
        val expectedJob = job(id = "job-1", category = software)
        val repository = FakeRepository(
            initialSnapshot = snapshot(
                jobs = listOf(
                    expectedJob,
                    job(id = "job-2", category = data)
                ),
                categories = listOf(software, data)
            )
        )
        val useCase = GetAllJobsInCategory(repository)

        val result = useCase.invoke(software)

        assertEquals(
            GetAllJobsInCategory.Result.Success(listOf(expectedJob)),
            result
        )
    }

    @Test
    fun `cache has no category returns success with empty list`() = runBlocking {
        val software = JobCategory("Software Engineering", 1)
        val data = JobCategory("Data Science", 0)
        val repository = FakeRepository(
            initialSnapshot = snapshot(
                jobs = listOf(job(id = "job-1", category = software)),
                categories = listOf(software)
            )
        )
        val useCase = GetAllJobsInCategory(repository)

        val result = useCase.invoke(data)

        assertEquals(
            GetAllJobsInCategory.Result.Success(emptyList()),
            result
        )
    }

    @Test
    fun `empty cache returns error`() = runBlocking {
        val repository = FakeRepository(initialSnapshot = null)
        val useCase = GetAllJobsInCategory(repository)

        val result = useCase.invoke(JobCategory("Software Engineering", 1))

        assertTrue(result is GetAllJobsInCategory.Result.Error)
        assertEquals(
            "Cache is empty",
            (result as GetAllJobsInCategory.Result.Error).t.message
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
}
