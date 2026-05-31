package com.quantax.gotogether

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.fcm.FcmTokenRepository
import com.quantax.gotogether.data.fcm.InAppNotificationObserver
import com.quantax.gotogether.data.fcm.NotificationChannels
import com.quantax.gotogether.data.fcm.NotificationPermissionState
import com.quantax.gotogether.navigation.GoTogetherNavGraph
import com.quantax.gotogether.navigation.Routes
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var notificationObserver: InAppNotificationObserver
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var fcmTokenRepository: FcmTokenRepository
    @Inject lateinit var permissionState: NotificationPermissionState

    private val pendingDeepLink = mutableStateOf<DeepLink?>(null)

    // Drives the observer's start/stop in sync with Firebase auth — fires synchronously
    // when registered, and again on every sign-in / sign-out within the same process.
    // Each fire also re-uploads the FCM token: a one-shot at sign-in isn't enough,
    // because tokens can be missing in Firestore for already-signed-in users.
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (!uid.isNullOrBlank()) {
            notificationObserver.start(uid)
            lifecycleScope.launch { fcmTokenRepository.registerCurrentToken(uid) }
        } else {
            notificationObserver.stop()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState.setDenied(!granted)
        if (!granted) {
            val permanentlyDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            val messageRes = if (permanentlyDenied) {
                R.string.notification_permission_settings_hint
            } else {
                R.string.notification_permission_denied
            }
            Toast.makeText(this, getString(messageRes), Toast.LENGTH_LONG).show()
            if (permanentlyDenied) {
                NotificationChannels.openAppNotificationSettings(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        pendingDeepLink.value = DeepLink.fromIntent(intent)
        auth.addAuthStateListener(authStateListener)
        setContent {
            GoTogetherTheme {
                val navController = rememberNavController()
                GoTogetherNavGraph(navController = navController)
                val link = pendingDeepLink.value
                LaunchedEffect(link) {
                    if (link != null) {
                        applyDeepLink(navController, link)
                        pendingDeepLink.value = null
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink.value = DeepLink.fromIntent(intent)
    }

    override fun onDestroy() {
        auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }

    private fun applyDeepLink(nav: NavHostController, link: DeepLink) {
        when (link.type) {
            NotificationChannels.TYPE_CHAT, NotificationChannels.TYPE_REQUEST_APPROVED -> {
                if (!link.chatroomId.isNullOrBlank()) {
                    nav.navigate(Routes.Chatroom.createRoute(link.chatroomId))
                }
            }
            NotificationChannels.TYPE_JOIN_REQUEST -> nav.navigate(Routes.Notifications.route)
            NotificationChannels.TYPE_TRIP_UPDATE -> {
                if (!link.tripId.isNullOrBlank()) {
                    nav.navigate(Routes.TripDetail.createRoute(link.tripId))
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        permissionState.setDenied(!granted)
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private data class DeepLink(
        val type: String,
        val tripId: String?,
        val chatroomId: String?
    ) {
        companion object {
            fun fromIntent(intent: Intent?): DeepLink? {
                val type = intent?.getStringExtra(NotificationChannels.EXTRA_TYPE) ?: return null
                val link = DeepLink(
                    type = type,
                    tripId = intent.getStringExtra(NotificationChannels.EXTRA_TRIP_ID),
                    chatroomId = intent.getStringExtra(NotificationChannels.EXTRA_CHATROOM_ID)
                )
                // Strip so configuration changes / re-entry don't replay this deep link.
                intent.removeExtra(NotificationChannels.EXTRA_TYPE)
                intent.removeExtra(NotificationChannels.EXTRA_TRIP_ID)
                intent.removeExtra(NotificationChannels.EXTRA_CHATROOM_ID)
                return link
            }
        }
    }
}
