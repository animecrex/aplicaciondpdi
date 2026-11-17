package com.dosis.app.screens.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dosis.app.DosisApplication
import com.dosis.app.data.model.Medication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DosisApplication).repository

    val allMedications: StateFlow<List<Medication>> = repository
        .getAllActiveMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
