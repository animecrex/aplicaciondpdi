package com.dosis.app.data.repository

import com.dosis.app.data.database.MedicationDao
import com.dosis.app.data.database.MedicationLogDao
import com.dosis.app.data.model.Medication
import com.dosis.app.data.model.MedicationLog
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio único de acceso a datos.
 * Abstrae la fuente de datos (Room) del resto de la aplicación.
 */
class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val logDao: MedicationLogDao
) {

    // ── Medicamentos ─────────────────────────────────────────────────────────

    fun getAllActiveMedications(): Flow<List<Medication>> =
        medicationDao.getAllActiveMedications()

    fun getAllMedications(): Flow<List<Medication>> =
        medicationDao.getAllMedications()

    suspend fun getMedicationById(id: Int): Medication? =
        medicationDao.getMedicationById(id)

    suspend fun getAllActiveMedicationsOnce(): List<Medication> =
        medicationDao.getAllActiveMedicationsOnce()

    suspend fun insertMedication(medication: Medication): Long =
        medicationDao.insertMedication(medication)

    suspend fun updateMedication(medication: Medication) =
        medicationDao.updateMedication(medication)

    suspend fun deleteMedication(medication: Medication) =
        medicationDao.deleteMedication(medication)

    suspend fun deleteMedicationById(id: Int) =
        medicationDao.deleteMedicationById(id)

    // ── Registros / Logs ─────────────────────────────────────────────────────

    suspend fun insertLog(log: MedicationLog): Long =
        logDao.insertLog(log)

    suspend fun updateLog(log: MedicationLog) =
        logDao.updateLog(log)

    fun getLogsForMedication(medicationId: Int): Flow<List<MedicationLog>> =
        logDao.getLogsForMedication(medicationId)

    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MedicationLog>> =
        logDao.getLogsForDay(startOfDay, endOfDay)

    suspend fun getLogForScheduledTime(medicationId: Int, scheduledTime: Long): MedicationLog? =
        logDao.getLogForScheduledTime(medicationId, scheduledTime)

    suspend fun deleteLogsForMedication(medicationId: Int) =
        logDao.deleteLogsForMedication(medicationId)
}
