package com.quantax.gotogether.presentation.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.quantax.gotogether.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthSuccess: Boolean = false,
    val verificationId: String? = null,
    val phoneNumber: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun getGoogleSignInIntent(context: Context): Intent {
        return authManager.getGoogleSignInIntent(context)
    }

    fun handleGoogleSignInResult(data: Intent?) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authManager.handleGoogleSignInResult(data)
            _state.value = _state.value.copy(isLoading = false)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isAuthSuccess = true)
                },
                onFailure = {
                    _state.value = _state.value.copy(error = it.message)
                }
            )
        }
    }

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                // Auto-verification handled if desired, but usually we just wait for onCodeSent
                _state.value = _state.value.copy(isLoading = false)
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                onFailure(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    verificationId = verificationId,
                    phoneNumber = phoneNumber
                )
                onCodeSent(verificationId)
            }
        }

        authManager.sendOtp(phoneNumber, activity, callbacks)
    }

    fun verifyOtp(verificationId: String, code: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        authManager.verifyOtp(verificationId, code) { result ->
            _state.value = _state.value.copy(isLoading = false)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isAuthSuccess = true)
                },
                onFailure = {
                    _state.value = _state.value.copy(error = it.message)
                }
            )
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun isLoggedIn(): Boolean = authManager.isLoggedIn()
}
