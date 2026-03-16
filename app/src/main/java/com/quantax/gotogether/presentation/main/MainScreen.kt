package com.quantax.gotogether.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quantax.gotogether.navigation.Routes
import com.quantax.gotogether.presentation.home.HomeScreen
import com.quantax.gotogether.presentation.mytrips.MyTripsScreen
import com.quantax.gotogether.presentation.notifications.NotificationsScreen
import com.quantax.gotogether.presentation.notifications.NotificationsViewModel
import com.quantax.gotogether.presentation.profile.ProfileScreen
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Routes.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Alerts", Routes.Notifications.route, Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomNavItem("My Trips", Routes.MyTrips.route, Icons.AutoMirrored.Filled.FormatListBulleted, Icons.AutoMirrored.Outlined.FormatListBulleted),
    BottomNavItem("Profile", Routes.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToChatroom: (String) -> Unit,
    onNavigateToPublicProfile: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val notificationsViewModel: NotificationsViewModel = hiltViewModel()
    val notificationsState by notificationsViewModel.state.collectAsState()
    val unreadCount = notificationsState.unreadCount

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        bottomBar = {
            NavigationBar(
                containerColor = GoTogetherTheme.colors.surfaceVariant,
                contentColor = GoTogetherTheme.colors.textPrimary,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                            if (item.route == Routes.Notifications.route && unreadCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = GoTogetherOrange) {
                                            Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                ) {
                                    Icon(icon, contentDescription = item.label)
                                }
                            } else {
                                Icon(icon, contentDescription = item.label)
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoTogetherOrange,
                            selectedTextColor = GoTogetherOrange,
                            unselectedIconColor = GoTogetherTheme.colors.textSecondary,
                            unselectedTextColor = GoTogetherTheme.colors.textSecondary,
                            indicatorColor = GoTogetherOrange.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    onNavigateToCreateTrip = onNavigateToCreateTrip,
                    onNavigateToTripDetail = onNavigateToTripDetail,
                    onNavigateToMessages = onNavigateToMessages,
                    onNavigateToPublicProfile = onNavigateToPublicProfile
                )
            }
            composable(Routes.Notifications.route) {
                NotificationsScreen(
                    viewModel = notificationsViewModel,
                    onNavigateToChatroom = onNavigateToChatroom
                )
            }
            composable(Routes.MyTrips.route) {
                MyTripsScreen(
                    onNavigateToDetail = onNavigateToTripDetail
                )
            }
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onSignOut = onSignOut
                )
            }
        }
    }
}
