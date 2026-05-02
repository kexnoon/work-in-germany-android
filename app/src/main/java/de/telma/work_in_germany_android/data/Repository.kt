package de.telma.work_in_germany_android.data

import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import kotlinx.coroutines.flow.StateFlow

interface Repository {
    val cache: StateFlow<JobCacheSnapshot?>

    suspend fun sync(forceRefresh: Boolean = false): JobCacheSnapshot

    suspend fun getCachedSnapshot(): JobCacheSnapshot?
}