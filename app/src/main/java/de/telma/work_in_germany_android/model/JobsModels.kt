package de.telma.work_in_germany_android.model

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
) {
    override fun equals(other: Any?): Boolean {
        return other is Job && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
