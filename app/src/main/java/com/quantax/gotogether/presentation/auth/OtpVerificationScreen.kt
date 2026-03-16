package com.quantax.gotogether.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherOrangeLight
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    verificationId: String,
    phoneNumber: String,
    authManager: AuthManager,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoTogetherTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
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
                )
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .padding(top = 40.dp, bottom = 32.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verify Phone",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoTogetherTheme.colors.textPrimary,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter the 6-digit code sent to",
                    fontSize = 15.sp,
                    color = GoTogetherTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = phoneNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoTogetherOrange,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("6-digit OTP") },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoTogetherOrange,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                        focusedLabelColor = GoTogetherOrange,
                        unfocusedLabelColor = GoTogetherTheme.colors.textSecondary,
                        cursorColor = GoTogetherOrange,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (otpCode.length == 6) {
                            isLoading = true
                            errorMessage = null
                            authManager.verifyOtp(verificationId, otpCode) { result ->
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
                        .height(58.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = GoTogetherOrange.copy(alpha = 0.5f),
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Verify & Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = { /* Implement resend logic if needed */ },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Didn't receive the code? Resend",
                        color = GoTogetherOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF5252).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = msg,
                            color = GoTogetherTheme.colors.textPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
