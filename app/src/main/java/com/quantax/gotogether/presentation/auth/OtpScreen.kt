package com.quantax.gotogether.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.quantax.gotogether.R
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.*

@Composable
fun OtpScreen(
    authManager: AuthManager,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }

    val countryCodes = listOf("+91" to "India", "+1" to "USA", "+44" to "UK", "+61" to "Australia", "+81" to "Japan")
    var selectedCountryCode by remember { mutableStateOf(countryCodes[0].first) }
    var expandedCountryMenu by remember { mutableStateOf(false) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification (e.g. instant verification on same device)
                isLoading = false
                authManager.verifyOtp(
                    verificationId ?: return,
                    credential.smsCode ?: return
                ) { result ->
                    result.fold(
                        onSuccess = { onAuthSuccess() },
                        onFailure = { errorMessage = it.message }
                    )
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                errorMessage = e.message ?: "Verification failed"
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                verificationId = vId
                codeSent = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GoTogetherTheme.colors.background, GoTogetherTheme.colors.surface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp),
                tint = GoTogetherOrange
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (codeSent) "Enter OTP" else "Phone Verification",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GoTogetherTheme.colors.textPrimary
            )

            Text(
                text = if (codeSent) "We sent a code to $phoneNumber"
                else "Enter your phone number to continue",
                fontSize = 14.sp,
                color = GoTogetherTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (!codeSent) {
                // Phone number input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable { expandedCountryMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCountryCode,
                                    fontWeight = FontWeight.Bold,
                                    color = GoTogetherTheme.colors.textPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Country Code",
                                    tint = GoTogetherTheme.colors.textSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = expandedCountryMenu,
                                onDismissRequest = { expandedCountryMenu = false },
                                modifier = Modifier.background(GoTogetherTheme.colors.surface)
                            ) {
                                countryCodes.forEach { (code, country) ->
                                    DropdownMenuItem(
                                        text = { Text("$code ($country)", color = GoTogetherTheme.colors.textPrimary) },
                                        onClick = {
                                            selectedCountryCode = code
                                            expandedCountryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoTogetherOrange,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.4f),
                        focusedLabelColor = GoTogetherOrange,
                        unfocusedLabelColor = GoTogetherTheme.colors.textSecondary,
                        cursorColor = GoTogetherOrange,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isNotBlank()) {
                            isLoading = true
                            errorMessage = null
                            val fullPhoneNumber = "$selectedCountryCode${phoneNumber.trim()}"
                            authManager.sendOtp(
                                phoneNumber = fullPhoneNumber,
                                activity = context as Activity,
                                callbacks = callbacks
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoTogetherOrange,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading && phoneNumber.isNotBlank()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_send),
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp),
                            tint = GoTogetherTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Send OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                // OTP input
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("6-digit OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoTogetherOrange,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.4f),
                        focusedLabelColor = GoTogetherOrange,
                        unfocusedLabelColor = GoTogetherTheme.colors.textSecondary,
                        cursorColor = GoTogetherOrange,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (otpCode.length == 6 && verificationId != null) {
                            isLoading = true
                            errorMessage = null
                            authManager.verifyOtp(verificationId!!, otpCode) { result ->
                                isLoading = false
                                result.fold(
                                    onSuccess = { onAuthSuccess() },
                                    onFailure = { errorMessage = it.message }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoTogetherOrange,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading && otpCode.length == 6
                ) {
                    Text(
                        text = "Verify & Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = GoTogetherOrange,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}