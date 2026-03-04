package com.quantax.gotogether.domain.model

import com.google.firebase.firestore.Exclude

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val bio: String = "",
    val profileImageUrl: String = ""
) {
    @get:Exclude
    val isVerified: Boolean
        get() = name.isNotBlank() &&
                email.isNotBlank() &&
                phone.isNotBlank() &&
                profileImageUrl.isNotBlank()
}
