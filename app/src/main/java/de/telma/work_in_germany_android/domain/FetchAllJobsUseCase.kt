package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory

class FetchAllJobsUseCase(
    private val repository: Repository
) {
    sealed interface Result {
        data class Success(val jobs: List<Job>, val categories: List<JobCategory>) : Result
        data object NoUpdates : Result
        data class Error(val t: Throwable) : Result
    }

    suspend operator fun invoke(): Result {
        return when (val syncResult = repository.sync()) {
            is Repository.CacheUpdateResult.Updated -> {
                val snapshot = repository.cache.value
                if (snapshot != null) {
                    Result.Success(
                        jobs = snapshot.jobs.all(),
                        categories = snapshot.categories
                    )
                } else {
                    Result.Error(Throwable("Cache is empty after update"))
                }
            }

            is Repository.CacheUpdateResult.Unchanged -> Result.NoUpdates

            is Repository.CacheUpdateResult.Error -> Result.Error(syncResult.t)
        }
    }
}