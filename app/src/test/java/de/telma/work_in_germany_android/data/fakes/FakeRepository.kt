package de.telma.work_in_germany_android.data.fakes

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRepository(
    initialSnapshot: JobCacheSnapshot? = null,
    val syncResult: Repository.CacheUpdateResult = Repository.CacheUpdateResult.Unchanged
) : Repository {
    private val cacheFlow = MutableStateFlow(initialSnapshot)

    override val cache: StateFlow<JobCacheSnapshot?> = cacheFlow

    override suspend fun sync(): Repository.CacheUpdateResult {
        return syncResult
    }
}