package de.telma.work_in_germany_android.model

data class JobsResponse (
    val total: Int,
    val lastUpdated: String,
    val jobs: List<Job>
)

data class Job(
    val id: String,
    val company: String,
    val companyType: String,
    val title: String,
    val location: String,
    val url: String,
    val postedAt: String,
    val category: JobCategory,
    val language: String,
    val visa: String,
    val source: String,
    val lastSeen: String,
    val firstSeen: String
)