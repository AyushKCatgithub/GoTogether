package com.quantax.gotogether.domain.model

data class JoinRequest(
    val id: String = "",
    val tripId: String = "",
    val tripTitle: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterImageUrl: String? = null,
    val hostId: String = "",
    val status: String = STATUS_PENDING,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_DENIED = "DENIED"
    }
}
