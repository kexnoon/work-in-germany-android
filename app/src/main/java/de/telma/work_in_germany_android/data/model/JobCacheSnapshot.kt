package de.telma.work_in_germany_android.data.model

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory

data class JobCacheSnapshot(
    val jobs: CachedJobs,
    val categories: List<JobCategory>,
    val lastUpdated: String
)

class CachedJobs private constructor(
    val allJobs: List<Job>,
    val jobsByCategory: Map<JobCategory, List<Job>>
) {
    fun all(): List<Job> = allJobs

    fun page(
        category: JobCategory? = null,
        offset: Int,
        limit: Int
    ): List<Job> {
        val source = category?.let { jobsByCategory[category] } ?: allJobs
        val from = offset.coerceAtLeast(0)
        val to = (offset + limit).coerceAtMost(source.size)
        return if (from < to) source.subList(from, to) else emptyList()
    }

    fun byCategory(category: JobCategory): List<Job> {
        return jobsByCategory[category] ?: emptyList()
    }

    fun byId(id: String): Job? {
        return allJobs.find { it.id == id }
    }

    companion object {
        fun from(jobs: List<Job>): CachedJobs {
            val uniqueJobs = jobs.distinctBy { it.id }

            val sortedAllJobs = uniqueJobs.sortedByDescending { it.postedAt }

            val sortedJobsByCategory = uniqueJobs
                .groupBy { it.category }
                .mapValues { (_, categoryJobs) ->
                    categoryJobs.sortedByDescending { it.postedAt }
                }

            return CachedJobs(
                allJobs = sortedAllJobs,
                jobsByCategory = sortedJobsByCategory
            )
        }
    }
}
