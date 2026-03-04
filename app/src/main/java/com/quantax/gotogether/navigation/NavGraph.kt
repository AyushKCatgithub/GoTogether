package com.quantax.gotogether.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.presentation.onboarding.isOnboardingCompleted
import com.quantax.gotogether.presentation.onboarding.saveOnboardingCompleted
import com.quantax.gotogether.presentation.auth.SignInScreen
import com.quantax.gotogether.presentation.auth.RegisterScreen
import com.quantax.gotogether.presentation.auth.OtpScreen
import com.quantax.gotogether.presentation.trip.CreateTripScreen
import com.quantax.gotogether.presentation.trip.TripDetailScreen
import com.quantax.gotogether.presentation.profile.EditProfileScreen
import com.quantax.gotogether.presentation.profile.PublicProfileScreen
import com.quantax.gotogether.presentation.messages.MessagesScreen
import com.quantax.gotogether.presentation.onboarding.OnboardingScreen
import com.quantax.gotogether.presentation.onboarding.SplashScreen
import com.quantax.gotogether.presentation.main.MainScreen

@Composable
fun GoTogetherNavGraph(
    navController: NavHostController,
    context: Context
) {
    val authManager = remember { AuthManager() }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen {
                val destination = when {
                    authManager.isLoggedIn() -> Routes.Main.route
                    isOnboardingCompleted(context) -> Routes.Register.route
                    else -> Routes.Onboarding.route
                }
                navController.navigate(destination) {
                    popUpTo(Routes.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Routes.Onboarding.route) {
            OnboardingScreen {
                saveOnboardingCompleted(context)
                navController.navigate(Routes.Register.route) {
                    popUpTo(Routes.Onboarding.route) { inclusive = true }
                }
            }
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                authManager = authManager,
                onNavigateToSignIn = {
                    navController.navigate(Routes.SignIn.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                },
                onNavigateToPhone = {
                    navController.navigate(Routes.Otp.route)
                },
                onAuthSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SignIn.route) {
            SignInScreen(
                authManager = authManager,
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToPhone = {
                    navController.navigate(Routes.Otp.route)
                },
                onAuthSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Otp.route) {
            OtpScreen(
                authManager = authManager,
                onAuthSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Otp.route) { inclusive = true }
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
                    navController.navigate(Routes.Register.route) {
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
