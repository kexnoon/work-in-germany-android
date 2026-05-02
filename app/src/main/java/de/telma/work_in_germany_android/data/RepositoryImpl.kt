package de.telma.work_in_germany_android.data

import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import de.telma.work_in_germany_android.network.RemoteDataSource
import de.telma.work_in_germany_android.storage.LocalDataSource
import kotlinx.coroutines.flow.StateFlow

class RepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : Repository {

    override val cache: StateFlow<JobCacheSnapshot>
        get() = TODO("Not yet implemented")

    override suspend fun sync(forceRefresh: Boolean): JobCacheSnapshot {
        TODO("Not yet implemented")
    }

    override suspend fun getCachedSnapshot(): JobCacheSnapshot {
        TODO("Not yet implemented")
    }
}