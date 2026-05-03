package de.telma.work_in_germany_android.data

import de.telma.work_in_germany_android.data.model.CachedJobs
import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import de.telma.work_in_germany_android.data.util.toDomain
import de.telma.work_in_germany_android.data.util.toDomainJobs
import de.telma.work_in_germany_android.network.RemoteDataSource
import de.telma.work_in_germany_android.storage.LocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : Repository {
    private val _cache = MutableStateFlow<JobCacheSnapshot?>(null)
    override val cache: StateFlow<JobCacheSnapshot?> = _cache.asStateFlow()

    override suspend fun sync(): Repository.CacheUpdateResult {
        var result: Repository.CacheUpdateResult = Repository.CacheUpdateResult.Unchanged
        try {
            val localFilesExist = localDataSource.checkIfFilesExist()
            if (!localFilesExist) {
                fetchRemote()
            } else {
                val localStats = localDataSource.getStats()
                val remoteStats = remoteDataSource.fetchStats().toDomain()

                if (localStats.lastUpdated != remoteStats.lastUpdated) {
                    fetchRemote()
                } else {
                    return Repository.CacheUpdateResult.Unchanged
                }
            }

            val localJobs = localDataSource.getJobs()
            val localStats = localDataSource.getStats()
            val categories = localStats.categories
            val cachedJobs = CachedJobs.from(localJobs)

            _cache.value = JobCacheSnapshot(
                jobs = cachedJobs,
                categories = categories,
                lastUpdated = localStats.lastUpdated
            )

            result = Repository.CacheUpdateResult.Updated

            return result
        } catch (t: Throwable) {
            return Repository.CacheUpdateResult.Error(t)
        }
    }

    private suspend fun fetchRemote() {
        val remoteStats = remoteDataSource.fetchStats().toDomain()
        val remoteJobs = remoteDataSource.fetchJobs().toDomainJobs(remoteStats.categories)

        localDataSource.saveJobs(remoteJobs)
        localDataSource.saveStats(remoteStats)

    }
}