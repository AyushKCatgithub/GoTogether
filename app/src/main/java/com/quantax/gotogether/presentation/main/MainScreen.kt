package com.quantax.gotogether.presentation.main

import com.quantax.gotogether.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quantax.gotogether.navigation.Routes
import com.quantax.gotogether.ui.theme.*
import com.quantax.gotogether.presentation.home.HomeScreen
import com.quantax.gotogether.presentation.profile.ProfileScreen
import com.quantax.gotogether.presentation.mytrips.MyTripsScreen
import com.quantax.gotogether.presentation.notifications.NotificationsScreen
import com.quantax.gotogether.presentation.notifications.NotificationsViewModel

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Routes.Home.route, Icons.Filled.Home, Icons.Filled.Home),
    BottomNavItem("Alerts", Routes.Notifications.route, Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomNavItem("My Trips", Routes.MyTrips.route, Icons.Filled.FormatListBulleted, Icons.Outlined.FormatListBulleted),
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
    
    val notificationsViewModel: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    Scaffold(
        containerColor = GoTogetherTheme.colors.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.Home.route,
                // Don't apply bottom padding here so the app content flows behind the floating bar
                modifier = Modifier.padding(bottom = 0.dp)
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
            
            // Custom Floating Navigation Bar mapped over the NavHost content
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(GoTogetherTheme.colors.navBar.copy(alpha = 0.85f)) // Frosty theme-colored glass
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(32.dp)
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(bounded = false, radius = 24.dp)
                            ) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    ) {
                        if (item.route == Routes.Notifications.route && unreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = GoTogetherOrange) {
                                        Text(text = unreadCount.toString(), color = Color.White)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) GoTogetherOrange else GoTogetherTheme.colors.textSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) GoTogetherOrange else GoTogetherTheme.colors.textSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GoTogetherOrange else TextGray
                        )
                    }
                }
            }
        }
    }
}
