package com.ritmo.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ritmo.app.ui.addedit.AddEditHabitRoute
import com.ritmo.app.ui.detail.HabitDetailRoute
import com.ritmo.app.ui.settings.SettingsRoute
import com.ritmo.app.ui.stats.StatsRoute
import com.ritmo.app.ui.today.TodayRoute

private object Routes {
    const val TODAY = "today"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ADD_EDIT = "addEditHabit?habitId={habitId}"
    const val DETAIL = "habit/{habitId}"
}

@Composable
fun RitmoApp(
    navController: NavHostController = rememberNavController(),
) {
    val topLevelRoutes = listOf(Routes.TODAY, Routes.STATS, Routes.SETTINGS)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                RitmoBottomBar(navController = navController)
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier,
        ) {
            composable(Routes.TODAY) {
                TodayRoute(
                    contentPadding = paddingValues,
                    onAddHabit = { navController.navigate("addEditHabit") },
                    onHabitClick = { habitId -> navController.navigate("habit/$habitId") },
                )
            }
            composable(Routes.STATS) {
                StatsRoute(contentPadding = paddingValues)
            }
            composable(Routes.SETTINGS) {
                SettingsRoute(contentPadding = paddingValues)
            }
            composable(
                route = Routes.ADD_EDIT,
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                AddEditHabitRoute(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(Routes.TODAY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
            ) {
                HabitDetailRoute(
                    onBack = { navController.popBackStack() },
                    onEdit = { habitId -> navController.navigate("addEditHabit?habitId=$habitId") },
                    onArchived = {
                        navController.navigate(Routes.TODAY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RitmoBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomItem(Routes.TODAY, "Hoje", { Icon(Icons.Filled.CheckCircle, contentDescription = null) }),
        BottomItem(Routes.STATS, "Estatísticas", { Icon(Icons.Filled.BarChart, contentDescription = null) }),
        BottomItem(Routes.SETTINGS, "Ajustes", { Icon(Icons.Filled.Settings, contentDescription = null) }),
    )
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = item.icon,
                label = { Text(item.label) },
            )
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)
