package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory

class GetAllJobsInCategory(
    private val repository: Repository
) {

    sealed interface Result {
        data class Success(val jobs: List<Job>) : Result
        data class Error(val t: Throwable) : Result
    }

    suspend fun invoke(category: JobCategory): Result {
        val snapshot = repository.cache.value
        return if (snapshot != null) {
            Result.Success(snapshot.jobs.byCategory(category))
        } else {
            Result.Error(Throwable("Cache is empty"))
        }
    }
}