package com.dosis.app

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dosis.app.navigation.DosisBottomNavigationBar
import com.dosis.app.navigation.DosisNavHost
import com.dosis.app.navigation.DosisScreen
import com.dosis.app.navigation.bottomNavDestinations
import com.dosis.app.ui.theme.DosisTheme

class MainActivity : ComponentActivity() {

    // Launcher para solicitar permiso de notificaciones (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permiso concedido o denegado; la app funciona de igual forma */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos necesarios
        requestRequiredPermissions()

        setContent {
            DosisTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Mostrar barra inferior solo en pantallas principales
                val showBottomBar = bottomNavDestinations.any { it.screen.route == currentRoute }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            DosisBottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    DosisNavHost(
                        navController = navController,
                    )
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        // Android 13+: permiso de notificaciones POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Android 12+: permiso de alarmas exactas SCHEDULE_EXACT_ALARM
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Abrir configuración para que el usuario otorgue el permiso manualmente
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}
