package de.telma.work_in_germany_android.domain
import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.model.Job

class GetJobByIdUseCase(
    repository: Repository
) {
    sealed interface Result {
        data class Success(val job: Job) : Result
        data class Error(val message: String) : Result
    }

    suspend fun invoke(id: String): Result {
        return Result.Error("Not implemented yet")
    }
}