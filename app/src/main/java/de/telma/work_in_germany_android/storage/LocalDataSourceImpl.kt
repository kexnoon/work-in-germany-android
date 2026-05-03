package de.telma.work_in_germany_android.storage

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

class LocalDataSourceImpl(
    private val rootDir: File,
    private val json: Json
) : LocalDataSource {

    private val statsFile = File(rootDir, STATS_FILE_NAME)
    private val jobsFile = File(rootDir, JOBS_FILE_NAME)

    override suspend fun checkIfFilesExist(): Boolean = withContext(Dispatchers.IO) {
        statsFile.isFile && jobsFile.isFile
    }

    @Throws(StatsRetrievalException::class)
    override suspend fun getStats(): Stats = withContext(Dispatchers.IO) {
        runCatching {
            json.decodeFromString<Stats>(statsFile.readText())
        }.getOrElse { throwable ->
            throw StatsRetrievalException(
                message = "Failed to read cached stats",
                cause = throwable
            )
        }
    }

    @Throws(JobsRetrievalException::class)
    override suspend fun getJobs(): List<Job> = withContext(Dispatchers.IO) {
        runCatching {
            json.decodeFromString<CachedJobsFile>(jobsFile.readText()).jobs
        }.getOrElse { throwable ->
            throw JobsRetrievalException(
                message = "Failed to read cached jobs",
                cause = throwable
            )
        }
    }

    @Throws(StatsStorageException::class)
    override suspend fun saveStats(stats: Stats) = withContext(Dispatchers.IO) {
        runCatching {
            statsFile.replace(json.encodeToString(stats))
        }.getOrElse { throwable ->
            throw StatsStorageException(
                message = "Failed to save cached stats",
                cause = throwable
            )
        }
    }

    @Throws(JobsStorageException::class)
    override suspend fun saveJobs(jobs: List<Job>) = withContext(Dispatchers.IO) {
        runCatching {
            jobsFile.replace(json.encodeToString(CachedJobsFile(jobs)))
        }.getOrElse { throwable ->
            throw JobsStorageException(
                message = "Failed to save cached jobs",
                cause = throwable
            )
        }
    }

    private fun File.replace(text: String) {
        parentFile?.mkdirs()

        val tempFile = File(parentFile, "$name.tmp")

        if (tempFile.exists() && !tempFile.delete()) {
            throw IllegalStateException("Failed to delete existing temp file: ${tempFile.absolutePath}")
        }

        tempFile.writeText(text)

        try {
            Files.move(tempFile.toPath(), toPath(), REPLACE_EXISTING, ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(tempFile.toPath(), toPath(), REPLACE_EXISTING)
        } catch (throwable: Throwable) {
            tempFile.delete()
            throw throwable
        }
    }

    @Serializable
    private data class CachedJobsFile(
        val jobs: List<Job>
    )

    private companion object {
        const val STATS_FILE_NAME = "stats.json"
        const val JOBS_FILE_NAME = "jobs.json"
    }
}
