package com.dosis.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dosis.app.data.model.Medication
import com.dosis.app.data.model.MedicationLog

@Database(
    entities = [Medication::class, MedicationLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DosisDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun medicationLogDao(): MedicationLogDao

    companion object {
        @Volatile
        private var INSTANCE: DosisDatabase? = null

        fun getInstance(context: Context): DosisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DosisDatabase::class.java,
                    "dosis_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
