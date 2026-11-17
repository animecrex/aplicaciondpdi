package com.dosis.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.dosis.app.screens.add.AddMedicationScreen
import com.dosis.app.screens.add.EditMedicationScreen
import com.dosis.app.screens.calendar.CalendarScreen
import com.dosis.app.screens.home.HomeScreen
import com.dosis.app.screens.reminders.RemindersScreen
import com.dosis.app.screens.wearable.WearableScreen

data class BottomNavDestination(
    val screen: DosisScreen,
    val label: String,
    val icon: ImageVector
)

val bottomNavDestinations = listOf(
    BottomNavDestination(DosisScreen.Home, "Inicio", Icons.Filled.Home),
    BottomNavDestination(DosisScreen.Calendar, "Calendario", Icons.Filled.CalendarMonth),
    BottomNavDestination(DosisScreen.Reminders, "Recordatorios", Icons.Filled.Notifications),
    BottomNavDestination(DosisScreen.WearableStatus, "Reloj", Icons.Filled.Watch)
)

@Composable
fun DosisNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = DosisScreen.Home.route
    ) {
        composable(DosisScreen.Home.route) {
            HomeScreen(
                onAddMedication = { navController.navigate(DosisScreen.AddMedication.route) },
                onMedicationClick = { medicationId ->
                    navController.navigate(DosisScreen.MedicationDetail.createRoute(medicationId))
                }
            )
        }
        composable(DosisScreen.Calendar.route) {
            CalendarScreen()
        }
        composable(DosisScreen.Reminders.route) {
            RemindersScreen()
        }
        composable(DosisScreen.WearableStatus.route) {
            WearableScreen()
        }
        composable(DosisScreen.AddMedication.route) {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = DosisScreen.EditMedication.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.IntType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getInt("medicationId") ?: return@composable
            EditMedicationScreen(
                medicationId = medicationId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = DosisScreen.MedicationDetail.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.IntType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getInt("medicationId") ?: return@composable
            // Reutilizamos EditMedicationScreen en modo detalle
            EditMedicationScreen(
                medicationId = medicationId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DosisBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.screen.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(destination.screen.route) {
                            popUpTo(DosisScreen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
