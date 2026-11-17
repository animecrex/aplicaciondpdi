package com.dosis.app.data.database

import androidx.room.*
import com.dosis.app.data.model.MedicationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog): Long

    @Update
    suspend fun updateLog(log: MedicationLog)

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY scheduledTime DESC")
    fun getLogsForMedication(medicationId: Int): Flow<List<MedicationLog>>

    @Query("""
        SELECT * FROM medication_logs 
        WHERE scheduledTime >= :startOfDay AND scheduledTime < :endOfDay 
        ORDER BY scheduledTime ASC
    """)
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun getLogForScheduledTime(medicationId: Int, scheduledTime: Long): MedicationLog?

    @Query("DELETE FROM medication_logs WHERE medicationId = :medicationId")
    suspend fun deleteLogsForMedication(medicationId: Int)
}
