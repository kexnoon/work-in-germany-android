package de.telma.work_in_germany_android.storage

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats

interface LocalDataSource {

    suspend fun checkIfFilesExist(): Boolean

    @Throws(StatsRetrievalException::class)
    suspend fun getStats(): Stats

    @Throws(JobsRetrievalException::class)
    suspend fun getJobs(): List<Job>

    @Throws(StatsStorageException::class)
    suspend fun saveStats(stats: Stats)

    @Throws(JobsStorageException::class)
    suspend fun saveJobs(jobs: List<Job>)
}

class StatsStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
class JobsStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
class StatsRetrievalException(message: String, cause: Throwable? = null) : Exception(message, cause)
class JobsRetrievalException(message: String, cause: Throwable? = null) : Exception(message, cause)