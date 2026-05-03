package de.telma.work_in_germany_android.data.util

import de.telma.work_in_germany_android.model.Job
import de.telma.work_in_germany_android.model.JobCategory
import de.telma.work_in_germany_android.model.Stats
import de.telma.work_in_germany_android.network.model.RemoteJob
import de.telma.work_in_germany_android.network.model.RemoteJobsResponse
import de.telma.work_in_germany_android.network.model.RemoteStats

fun RemoteStats.toDomain(): Stats {
    return Stats(
        totalJobs = totalJobs,
        visaFriendly = visaFriendly,
        englishFriendly = englishFriendly,
        companiesTracked = companiesTracked,
        categories = categories.map { (name, amount) ->
            JobCategory(
                name = name,
                amount = amount
            )
        },
        lastUpdated = lastUpdated
    )
}

fun RemoteJobsResponse.toDomainJobs(stats: Stats): List<Job> {
    return toDomainJobs(stats.categories)
}

fun RemoteJobsResponse.toDomainJobs(categories: List<JobCategory>): List<Job> {
    val categoriesByName = categories.associateBy { it.name }

    return jobs.map { remoteJob ->
        remoteJob.toDomain(categoriesByName)
    }
}

private fun RemoteJob.toDomain(categoriesByName: Map<String, JobCategory>): Job {
    return Job(
        id = id,
        company = company,
        companyType = companyType,
        title = title,
        location = location,
        url = url,
        postedAt = postedAt,
        category = categoriesByName[category] ?: JobCategory(
            name = category,
            amount = 0
        ),
        language = language,
        visa = visa,
        source = source,
        lastSeen = lastSeen,
        firstSeen = firstSeen
    )
}
