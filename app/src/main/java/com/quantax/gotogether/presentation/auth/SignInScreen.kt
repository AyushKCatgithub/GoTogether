package com.quantax.gotogether.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.quantax.gotogether.R
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherOrangeLight
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.TextGray

@Composable
fun SignInScreen(
    onNavigateToOtp: (String, String) -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    val countryCodes = listOf("+91" to "India", "+1" to "USA", "+44" to "UK", "+61" to "Australia", "+81" to "Japan")
    var selectedCountryCode by remember { mutableStateOf(countryCodes[0].first) }

    LaunchedEffect(state.isAuthSuccess) {
        if (state.isAuthSuccess) {
            onAuthSuccess()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
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

            PhoneSignInSection(
                phoneNumber = phoneNumber,
                selectedCountryCode = selectedCountryCode,
                countryCodes = countryCodes,
                isLoading = state.isLoading,
                onPhoneNumberChange = { 
                    if (it.length <= 10) {
                        phoneNumber = it
                    }
                },
                onCountryCodeChange = { 
                    selectedCountryCode = it
                },
                onSendOtp = {
                    if (phoneNumber.length < 10) {
                        // We can handle local validation here or in VM
                    } else {
                        val fullPhoneNumber = "$selectedCountryCode${phoneNumber.trim()}"
                        viewModel.sendOtp(
                            phoneNumber = fullPhoneNumber,
                            activity = context as Activity,
                            onCodeSent = { vId ->
                                onNavigateToOtp(vId, fullPhoneNumber)
                            },
                            onFailure = {}
                        )
                    }
                }
            )

            state.error?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                ErrorBanner(message = msg)
            }

            DividerWithText()

            GoogleSignInSection(
                isLoading = state.isLoading,
                onGoogleSignIn = {
                    try {
                        val intent = viewModel.getGoogleSignInIntent(context)
                        googleSignInLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Handled by VM or locally
                    }
                }
            )

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = GoTogetherOrange,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
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
            placeholder = { Text("Enter number") },
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
                .height(58.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = GoTogetherOrange.copy(alpha = 0.4f),
                    spotColor = GoTogetherOrange.copy(alpha = 0.8f)
                ),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading,
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
            .height(58.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.2.dp, GoTogetherTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
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
fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF5252).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = message,
            color = GoTogetherTheme.colors.textPrimary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
