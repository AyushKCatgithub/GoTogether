package com.quantax.gotogether.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    val currentUser get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() = auth.signOut()
    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(context.getString(com.quantax.gotogether.R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        client.signOut() // Clear cache
        return client.signInIntent
    }

    suspend fun handleGoogleSignInResult(data: android.content.Intent?): Result<Unit> {
        return try {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken ?: throw Exception("No ID Token found")

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential)
                .also { t ->
                    while (!t.isComplete) {
                        kotlinx.coroutines.delay(100)
                    }
                }

            if (authResult.isSuccessful) {
                ensureProfileExists()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Firebase sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureProfileExists() {
        val user = auth.currentUser ?: return
        val profileRepo = UserProfileRepository()

        val existingProfileResult = profileRepo.getProfile(user.uid)
        val profile = existingProfileResult.getOrNull()

        if (profile == null) {
            val newProfile = UserProfile(
                uid = user.uid,
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                name = user.displayName ?: "" // Optionally capture their Google name if present
            )
            profileRepo.saveProfile(newProfile)
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
                        ensureProfileExists()
                        onResult(Result.success(Unit))
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
