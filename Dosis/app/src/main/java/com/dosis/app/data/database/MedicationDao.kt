package com.dosis.app.data.database

import androidx.room.*
import com.dosis.app.data.model.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    // ── Consultas ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): Medication?

    @Query("SELECT * FROM medications WHERE isActive = 1")
    suspend fun getAllActiveMedicationsOnce(): List<Medication>

    // ── Operaciones CRUD ─────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("UPDATE medications SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMedication(id: Int)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Int)
}
