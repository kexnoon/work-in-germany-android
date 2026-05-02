package de.telma.work_in_germany_android.model

import kotlinx.serialization.SerialName

data class JobCategory (
    val name: String,
    val amount: Int
) {
    override fun equals(other: Any?): Boolean = other is JobCategory && name == other.name

    override fun hashCode(): Int = name.hashCode()
}

data class Stats (
    @SerialName("total_jobs")
    val totalJobs: Int,
    @SerialName("visa_friendly")
    val visaFriendly: Int,
    @SerialName("english_friendly")
    val englishFriendly: Int,
    @SerialName("companies_tracked")
    val companiesTracked: Int,
    val categories: List<JobCategory>,
    @SerialName("last_updated")
    val lastUpdated: String
)
