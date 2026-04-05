package com.quantax.gotogether.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val unselectedIcon: ImageVector,
    val isAction: Boolean = false
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Routes.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(
        "Alerts",
        Routes.Notifications.route,
        Icons.Filled.Notifications,
        Icons.Outlined.Notifications
    ),
    BottomNavItem("Create", "action_create", Icons.Filled.Add, Icons.Filled.Add, isAction = true),
    BottomNavItem(
        "My Trips",
        Routes.MyTrips.route,
        Icons.AutoMirrored.Filled.FormatListBulleted,
        Icons.AutoMirrored.Outlined.FormatListBulleted
    ),
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
    val notificationsState by notificationsViewModel.state.collectAsStateWithLifecycle()
    val unreadCount = notificationsState.unreadCount

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = GoTogetherTheme.colors.textPrimary,
                    tonalElevation = 0.dp,
                    windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (item.isAction) {
                                    onNavigateToCreateTrip()
                                } else {
                                    if (!isSelected) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = {
                                val icon =
                                    if (isSelected) item.selectedIcon else item.unselectedIcon
                                if (item.isAction) {
                                    Surface(
                                        modifier = Modifier.size(52.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = GoTogetherOrange,
                                        shadowElevation = 8.dp
                                    ) {
                                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                            Icon(
                                                item.selectedIcon,
                                                contentDescription = item.label,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                } else if (item.route == Routes.Notifications.route && unreadCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = GoTogetherOrange) {
                                                Text(
                                                    unreadCount.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp
                                                )
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
                                if (!item.isAction) {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoTogetherOrange,
                                selectedTextColor = GoTogetherOrange,
                                unselectedIconColor = GoTogetherTheme.colors.textSecondary,
                                unselectedTextColor = GoTogetherTheme.colors.textSecondary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }

            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    onNavigateToDetail = onNavigateToTripDetail,
                    onNavigateToPublicProfile = onNavigateToPublicProfile
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
