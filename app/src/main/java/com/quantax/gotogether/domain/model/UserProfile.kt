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
    val profileImageUrl: String = "",
    val verificationPhotoUrls: List<String> = emptyList(),
    /** Normalized (lowercase) username for uniqueness checks; set when saving. */
    val usernameLower: String = ""
) {
    /** At most 4 background/verification photos; used for profile header and verification. */
    val backgroundPhotoUrls: List<String>
        get() = verificationPhotoUrls.take(4)

    @get:Exclude
    val isVerified: Boolean
        get() = verificationPhotoUrls.filter { it.isNotBlank() }.size >= 3
}
