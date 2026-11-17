package com.dosis.app.navigation

/**
 * Define todas las rutas de navegación de la aplicación Dosis.
 */
sealed class DosisScreen(val route: String) {
    // Pantallas principales (barra inferior)
    object Home : DosisScreen("home")
    object Calendar : DosisScreen("calendar")
    object Reminders : DosisScreen("reminders")
    object WearableStatus : DosisScreen("wearable_status")

    // Pantallas secundarias
    object AddMedication : DosisScreen("add_medication")
    object EditMedication : DosisScreen("edit_medication/{medicationId}") {
        fun createRoute(medicationId: Int) = "edit_medication/$medicationId"
    }
    object MedicationDetail : DosisScreen("medication_detail/{medicationId}") {
        fun createRoute(medicationId: Int) = "medication_detail/$medicationId"
    }
}

/** Ítems de la barra de navegación inferior */
data class BottomNavItem(
    val screen: DosisScreen,
    val title: String,
    val iconResName: String // nombre del ícono de Material Icons
)
