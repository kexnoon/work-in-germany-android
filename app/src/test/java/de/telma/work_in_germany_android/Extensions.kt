package de.telma.work_in_germany_android

import de.telma.work_in_germany_android.data.model.CachedJobs
import de.telma.work_in_germany_android.data.model.JobCacheSnapshot
import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory
import de.telma.work_in_germany_android.model.Stats
import de.telma.work_in_germany_android.network.model.RemoteJob
import de.telma.work_in_germany_android.network.model.RemoteJobsMetadata
import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats

fun stats(
    lastUpdated: String = "2026-05-03 12:00 UTC",
    categories: List<JobCategory> = listOf(JobCategory("Software Engineering", 1))
): Stats {
    return Stats(
        totalJobs = categories.sumOf { it.amount },
        visaFriendly = 0,
        englishFriendly = 0,
        companiesTracked = 0,
        categories = categories,
        lastUpdated = lastUpdated
    )
}

fun remoteStats(
    lastUpdated: String = "2026-05-03 12:00 UTC",
    categories: Map<String, Int> = mapOf("Software Engineering" to 1)
): RemoteStats {
    return RemoteStats(
        totalJobs = categories.values.sum(),
        visaFriendly = 0,
        englishFriendly = 0,
        companiesTracked = 0,
        categories = categories,
        lastUpdated = lastUpdated
    )
}

fun job(
    id: String,
    category: JobCategory = JobCategory("Software Engineering", 1),
    postedAt: String = "2026-05-03"
): Job {
    return Job(
        id = id,
        company = "Company $id",
        companyType = "product",
        title = "Android Developer",
        location = "Berlin",
        url = "https://example.com/$id",
        postedAt = postedAt,
        category = category,
        language = "english",
        visa = "yes",
        source = "source",
        lastSeen = "2026-05-03T12:00:00+00:00",
        firstSeen = "2026-05-03T12:00:00+00:00"
    )
}

fun remoteJobsResponse(vararg jobs: RemoteJob): RemoteJobsResponse {
    return RemoteJobsResponse(
        metadata = RemoteJobsMetadata(
            total = jobs.size,
            lastUpdated = "2026-05-03 12:00 UTC"
        ),
        jobs = jobs.toList()
    )
}

fun remoteJob(
    id: String,
    category: String = "Software Engineering",
    postedAt: String = "2026-05-03"
): RemoteJob {
    return RemoteJob(
        id = id,
        company = "Company $id",
        companyType = "product",
        title = "Android Developer",
        location = "Berlin",
        url = "https://example.com/$id",
        postedAt = postedAt,
        category = category,
        language = "english",
        visa = "yes",
        source = "source",
        lastSeen = "2026-05-03T12:00:00+00:00",
        firstSeen = "2026-05-03T12:00:00+00:00"
    )
}

fun snapshot(
    jobs: List<Job>,
    categories: List<JobCategory>
): JobCacheSnapshot {
    return JobCacheSnapshot(
        jobs = CachedJobs.from(jobs),
        categories = categories,
        lastUpdated = "2026-05-04 12:00 UTC"
    )
}
