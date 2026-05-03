package de.telma.work_in_germany_android.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteJobsResponse(
    val metadata: RemoteJobsMetadata,
    val jobs: List<RemoteJob>
)

@Serializable
data class RemoteJobsMetadata(
    val total: Int,

    @SerialName("last_updated")
    val lastUpdated: String
)

@Serializable
data class RemoteJob(
    val id: String,
    val company: String,

    @SerialName("company_type")
    val companyType: String,

    val title: String,
    val location: String,
    val url: String,

    @SerialName("posted_at")
    val postedAt: String,

    val category: String,
    val language: String,
    val visa: String,
    val source: String,

    @SerialName("last_seen")
    val lastSeen: String,

    @SerialName("first_seen")
    val firstSeen: String
)

@Serializable
data class RemoteStats(
    @SerialName("total_jobs")
    val totalJobs: Int,

    @SerialName("visa_friendly")
    val visaFriendly: Int,

    @SerialName("english_friendly")
    val englishFriendly: Int,

    @SerialName("companies_tracked")
    val companiesTracked: Int,

    val categories: Map<String, Int>,

    @SerialName("last_updated")
    val lastUpdated: String
)
