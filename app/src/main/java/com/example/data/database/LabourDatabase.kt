package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.LabourDao
import com.example.data.model.Labour
import com.example.data.model.Attendance
import com.example.data.model.AdvancePayment

@Database(
    entities = [Labour::class, Attendance::class, AdvancePayment::class],
    version = 1,
    exportSchema = false
)
abstract class LabourDatabase : RoomDatabase() {
    abstract fun labourDao(): LabourDao

    companion object {
        @Volatile
        private var INSTANCE: LabourDatabase? = null

        fun getDatabase(context: Context): LabourDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LabourDatabase::class.java,
                    "labour_hisab_kitab_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
