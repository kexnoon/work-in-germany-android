package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.fakes.FakeRepository
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
}
