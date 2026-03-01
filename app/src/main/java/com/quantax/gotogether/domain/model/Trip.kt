package com.quantax.gotogether.domain.model

data class Trip(
    val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostImageUrl: String? = null,
    val hostVerified: Boolean = false,
    val title: String = "",
    val startingLocation: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val budgetRange: String = "",
    val groupSize: Int = 0,
    val joinedCount: Int = 0,
    val interests: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<String> = emptyList()
)
