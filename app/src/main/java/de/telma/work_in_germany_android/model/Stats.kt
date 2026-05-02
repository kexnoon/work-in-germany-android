package de.telma.work_in_germany_android.model

data class JobCategory (
    val name: String,
    val amount: Int
)

data class Stats (
    val totalJobs: Int,
    val visaFriendly: Int,
    val englishFriendly: Int,
    val companiesTracked: Int,
    val categories: List<JobCategory>,
    val lastUpdated: String
)
