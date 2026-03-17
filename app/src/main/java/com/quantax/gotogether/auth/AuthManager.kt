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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val profileRepo: UserProfileRepository
) {
    
    val currentUser get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() = auth.signOut()
    
    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(context.getString(com.quantax.gotogether.R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut() // Clear cache
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
            // Any exceptions from task.getResult(), suspension, or ensureProfileExists() will be caught here
            Result.failure(e)
        }
    }

    private suspend fun ensureProfileExists() {
        try {
            val user = auth.currentUser ?: return

            val existingProfileResult = profileRepo.getProfile(user.uid)
            val profile = existingProfileResult.getOrNull()

            if (profile == null) {
                val newProfile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    phone = user.phoneNumber ?: "",
                    name = user.displayName ?: "" // Optionally capture their Google name if present
                )
                val result = profileRepo.saveProfile(newProfile)
                if (result.isFailure) {
                    throw Exception("Failed to save user profile: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            // Re-throw to be handled by caller
            throw e
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

    fun verifyOtp(
        verificationId: String,
        code: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            ensureProfileExists()
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                onResult(Result.success(Unit))
                            }
                        } catch (e: Exception) {
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                onResult(Result.failure(e))
                            }
                        }
                    }
                } else {
                    onResult(
                        Result.failure(
                            task.exception ?: Exception("OTP verification failed")
                        )
                    )
                }
            }
    }
}
