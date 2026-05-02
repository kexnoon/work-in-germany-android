package de.telma.work_in_germany_android.model

import kotlinx.serialization.SerialName

data class JobsResponse (
    val meta: JobsMetaData,
    val jobs: List<Job>
)

data class JobsMetaData (
    val total: Int,
    @SerialName("last_updated")
    val lastUpdated: String
)

data class Job(
    val id: String,
    val company: String,
    @SerialName("company_type")
    val companyType: String,
    val title: String,
    val location: String,
    val url: String,
    @SerialName("posted_at")
    val postedAt: String,
    val category: JobCategory,
    val language: String,
    val visa: String,
    val source: String,
    @SerialName("last_seen")
    val lastSeen: String,
    @SerialName("first_seen")
    val firstSeen: String
) {
    override fun equals(other: Any?): Boolean {
        return other is Job && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}