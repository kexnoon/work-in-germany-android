package de.telma.work_in_germany_android.domain

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.model.Job

class FetchJobsUseCase(
    private val repository: Repository
) {
    sealed interface Result {
        data class Success(val jobs: List<Job>) : Result
        data object NoUpdates : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke() {}
}