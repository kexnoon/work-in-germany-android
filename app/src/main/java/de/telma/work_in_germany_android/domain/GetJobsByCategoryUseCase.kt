package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory

class GetJobsByCategoryUseCase(
    private val repository: Repository
) {

    sealed interface Result {
        data class Success(val jobs: List<Job>) : Result
        data class Error(val message: String) : Result
    }

    suspend fun invoke(category: JobCategory): Result {
        return Result.Error("Not implemented yet")
    }
}