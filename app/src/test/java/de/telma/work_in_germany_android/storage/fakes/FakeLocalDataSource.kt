package de.telma.work_in_germany_android.storage.fakes

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats
import de.telma.work_in_germany_android.stats
import de.telma.work_in_germany_android.storage.LocalDataSource

class FakeLocalDataSource(
    private var stats: Stats = stats(),
    private var jobs: List<Job> = emptyList(),
    private val filesExist: Boolean = true,
    private val saveJobsThrowable: Throwable? = null,
    private val saveStatsThrowable: Throwable? = null
) : LocalDataSource {
    var saveJobsCalls = 0
        private set

    var saveStatsCalls = 0
        private set

    override suspend fun checkIfFilesExist(): Boolean = filesExist

    override suspend fun getStats(): Stats = stats

    override suspend fun getJobs(): List<Job> = jobs

    override suspend fun saveJobs(jobs: List<Job>) {
        saveJobsCalls++
        saveJobsThrowable?.let { throw it }
        this.jobs = jobs
    }

    override suspend fun saveStats(stats: Stats) {
        saveStatsCalls++
        saveStatsThrowable?.let { throw it }
        this.stats = stats
    }
}
