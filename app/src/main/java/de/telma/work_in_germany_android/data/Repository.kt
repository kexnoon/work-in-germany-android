package de.telma.work_in_germany_android.data

import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import kotlinx.coroutines.flow.StateFlow

interface Repository {
    val cache: StateFlow<JobCacheSnapshot?>

    suspend fun sync(): CacheUpdateResult

    sealed interface CacheUpdateResult {
        data object Updated : CacheUpdateResult
        data object Unchanged : CacheUpdateResult
        data class Error(val t: Throwable) : CacheUpdateResult
    }

}