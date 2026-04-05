package com.quantax.gotogether.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val profileRepo: UserProfileRepository
) {

    val currentUser get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() = auth.signOut()

    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.quantax.gotogether.R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut() // Clear cached account so the chooser always shows
        return client.signInIntent
    }

    suspend fun handleGoogleSignInResult(data: android.content.Intent?): Result<Unit> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken ?: throw Exception("No ID Token found")

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            suspendCancellableCoroutine { continuation ->
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            continuation.resume(authTask.result)
                        } else {
                            continuation.resumeWithException(
                                authTask.exception ?: Exception("Firebase sign-in failed")
                            )
                        }
                    }
            }

            ensureProfileExists()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureProfileExists() {
        val user = auth.currentUser ?: return
        val existingProfile = profileRepo.getProfile(user.uid).getOrNull()
        if (existingProfile == null) {
            val newProfile = UserProfile(
                uid = user.uid,
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                name = user.displayName ?: "",
                profileImageUrl = user.photoUrl?.toString() ?: ""
            )
            profileRepo.saveProfile(newProfile)
        } else if (existingProfile.profileImageUrl.isBlank() && user.photoUrl != null) {
            // Update existing profile with photo if it's currently missing
            profileRepo.saveProfile(existingProfile.copy(profileImageUrl = user.photoUrl.toString()))
        }
    }

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Suspending OTP verification — must be called from a coroutine scope tied to the ViewModel
     * (e.g. viewModelScope) so that if the user navigates away, the coroutine is cancelled and
     * the lifecycle-unbound raw CoroutineScope leak is avoided.
     */
    suspend fun verifyOtp(verificationId: String, code: String): Result<Unit> {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return try {
            suspendCancellableCoroutine { continuation ->
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("OTP verification failed")
                            )
                        }
                    }
            }
            ensureProfileExists()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
