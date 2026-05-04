package de.telma.work_in_germany_android.domain
import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.model.Job

class GetJobByIdUseCase(
    private val repository: Repository
) {
    sealed interface Result {
        data class Success(val job: Job) : Result
        data class Error(val t: Throwable) : Result
    }

    suspend fun invoke(id: String): Result {
        val snapshot = repository.cache.value
        return if (snapshot != null) {
            val job = snapshot.jobs.byId(id)
            if (job != null) {
                Result.Success(job)
            } else {
                Result.Error(Throwable("Job with id $id not found"))
            }
        } else {
            Result.Error(Throwable("Cache is empty"))
        }
    }
}