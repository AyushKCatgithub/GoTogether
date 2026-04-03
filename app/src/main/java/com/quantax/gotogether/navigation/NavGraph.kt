package com.quantax.gotogether.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quantax.gotogether.presentation.auth.AuthViewModel
import com.quantax.gotogether.presentation.auth.OtpVerificationScreen
import com.quantax.gotogether.presentation.auth.SignInScreen
import com.quantax.gotogether.presentation.main.MainScreen
import com.quantax.gotogether.presentation.messages.MessagesScreen
import com.quantax.gotogether.presentation.onboarding.SplashScreen
import com.quantax.gotogether.presentation.profile.EditProfileScreen
import com.quantax.gotogether.presentation.profile.PublicProfileScreen
import com.quantax.gotogether.presentation.trip.CreateTripScreen
import com.quantax.gotogether.presentation.trip.TripDetailScreen
import java.net.URLDecoder

@Composable
fun GoTogetherNavGraph(
    navController: NavHostController,
    context: Context
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            SplashScreen {
                val destination = if (authViewModel.isLoggedIn()) {
                    Routes.Main.route
                } else {
                    Routes.SignIn.route
                }
                navController.navigate(destination) {
                    popUpTo(Routes.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Routes.SignIn.route) {
            SignInScreen(
                onNavigateToOtp = { vId, phone ->
                    navController.navigate(Routes.Otp.createRoute(vId, phone))
                },
                onAuthSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.Otp.route,
            arguments = listOf(
                navArgument("verificationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("phoneNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val vId = backStackEntry.arguments?.getString("verificationId")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            val phone = backStackEntry.arguments?.getString("phoneNumber")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            OtpVerificationScreen(
                verificationId = vId,
                phoneNumber = phone,
                onNavigateBack = { navController.popBackStack() },
                onAuthSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main.route) {
            MainScreen(
                rootNavController = navController,
                onNavigateToCreateTrip = {
                    navController.navigate(Routes.CreateTrip.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Routes.EditProfile.route)
                },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(Routes.TripDetail.createRoute(tripId))
                },
                onNavigateToMessages = {
                    navController.navigate(Routes.Messages.route)
                },
                onNavigateToChatroom = { chatroomId ->
                    navController.navigate(Routes.Chatroom.createRoute(chatroomId))
                },
                onNavigateToPublicProfile = { userId ->
                    navController.navigate(Routes.PublicProfile.createRoute(userId))
                },
                onSignOut = {
                    navController.navigate(Routes.SignIn.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Messages.route) {
            MessagesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChatroom = { chatroomId ->
                    navController.navigate(Routes.Chatroom.createRoute(chatroomId))
                }
            )
        }

        composable(Routes.CreateTrip.route) {
            CreateTripScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Routes.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripDetailScreen(
                tripId = tripId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChatroom = { chatroomId ->
                    navController.navigate(Routes.Chatroom.createRoute(chatroomId))
                },
                onNavigateToPublicProfile = { userId ->
                    navController.navigate(Routes.PublicProfile.createRoute(userId))
                }
            )
        }

        composable(
            Routes.Chatroom.route,
            arguments = listOf(navArgument("chatroomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatroomId = backStackEntry.arguments?.getString("chatroomId") ?: ""
            com.quantax.gotogether.presentation.chat.ChatroomScreen(
                chatroomId = chatroomId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Routes.PublicProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PublicProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
