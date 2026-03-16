package com.quantax.gotogether.presentation.auth

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherOrangeLight
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    authManager: AuthManager,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }

    val countryCodes = listOf("+91" to "India", "+1" to "USA", "+44" to "UK", "+61" to "Australia", "+81" to "Japan")
    var selectedCountryCode by remember { mutableStateOf(countryCodes[0].first) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            val authResult = authManager.handleGoogleSignInResult(result.data)
            isLoading = false
            authResult.fold(
                onSuccess = { onAuthSuccess() },
                onFailure = { errorMessage = it.message }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GoTogetherTheme.colors.background,
                        GoTogetherTheme.colors.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        GlowingBackgroundOrb()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 100.dp, bottom = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection()

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = !codeSent,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PhoneSignInSection(
                        phoneNumber = phoneNumber,
                        selectedCountryCode = selectedCountryCode,
                        countryCodes = countryCodes,
                        isLoading = isLoading,
                        onPhoneNumberChange = { phoneNumber = it },
                        onCountryCodeChange = { selectedCountryCode = it },
                        onSendOtp = {
                            if (phoneNumber.isNotBlank()) {
                                isLoading = true
                                errorMessage = null
                                val fullPhoneNumber = "$selectedCountryCode${phoneNumber.trim()}"
                                authManager.sendOtp(
                                    phoneNumber = fullPhoneNumber,
                                    activity = context as Activity,
                                    callbacks = callbacks
                                )
                            } else {
                                errorMessage = "Please enter a valid phone number"
                            }
                        }
                    )

                    DividerWithText()

                    GoogleSignInSection(
                        isLoading = isLoading,
                        onGoogleSignIn = {
                            isLoading = true
                            errorMessage = null
                            try {
                                val intent = authManager.getGoogleSignInIntent(context)
                                googleSignInLauncher.launch(intent)
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.message
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = codeSent,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 })
            ) {
                OtpVerificationSection(
                    otpCode = otpCode,
                    isLoading = isLoading,
                    onOtpChange = { if (it.length <= 6) otpCode = it },
                    onVerify = {
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
                        } else {
                            errorMessage = "Please enter a valid 6-digit OTP"
                        }
                    },
                    onEditPhone = {
                        codeSent = false
                        otpCode = ""
                        errorMessage = null
                    }
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = GoTogetherOrange,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(24.dp))
                ErrorBanner(message = msg)
            }
        }
    }
}

@Composable
fun GlowingBackgroundOrb() {
    Box(
        modifier = Modifier
            .padding(top = 40.dp)
            .size(300.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(GoTogetherOrange.copy(alpha = 0.2f), Color.Transparent),
                    radius = 400f
                ),
                shape = CircleShape
            )
    )
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White.copy(alpha = 0.05f), CircleShape)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(70.dp),
            tint = GoTogetherOrange
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Join GoTogether",
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GoTogetherTheme.colors.textPrimary,
        letterSpacing = (-1.5).sp
    )

    Text(
        text = "Discover journeys. Log in or sign up seamlessly.",
        fontSize = 15.sp,
        color = GoTogetherTheme.colors.textSecondary,
        modifier = Modifier.padding(top = 8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun PhoneSignInSection(
    phoneNumber: String,
    selectedCountryCode: String,
    countryCodes: List<Pair<String, String>>,
    isLoading: Boolean,
    onPhoneNumberChange: (String) -> Unit,
    onCountryCodeChange: (String) -> Unit,
    onSendOtp: () -> Unit
) {
    var expandedCountryMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Box {
                    Row(
                        modifier = Modifier
                            .clickable(enabled = !isLoading) { expandedCountryMenu = true }
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
                                    onCountryCodeChange(code)
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
            onClick = onSendOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = GoTogetherOrange.copy(alpha = 0.4f),
                    spotColor = GoTogetherOrange.copy(alpha = 0.8f)
                ),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading && phoneNumber.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(GoTogetherOrange, GoTogetherOrangeLight))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Send OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DividerWithText() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f))
        )
        Text(
            text = "OR",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = GoTogetherTheme.colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun GoogleSignInSection(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit
) {
    OutlinedButton(
        onClick = onGoogleSignIn,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, GoTogetherTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GoTogetherTheme.colors.card,
            contentColor = GoTogetherTheme.colors.textPrimary
        ),
        enabled = !isLoading
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun OtpVerificationSection(
    otpCode: String,
    isLoading: Boolean,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onEditPhone: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = otpCode,
            onValueChange = onOtpChange,
            label = { Text("6-digit OTP") },
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
            onClick = onVerify,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = GoTogetherOrange.copy(alpha = 0.4f),
                    spotColor = GoTogetherOrange.copy(alpha = 0.8f)
                ),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading && otpCode.length == 6,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(GoTogetherOrange, GoTogetherOrangeLight))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Verify & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onEditPhone, enabled = !isLoading) {
            Text(
                text = "Edit Phone Number",
                color = GoTogetherOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF5252).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFFFF5252),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
