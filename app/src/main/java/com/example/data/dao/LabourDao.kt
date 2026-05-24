package com.example.data.dao

import androidx.room.*
import com.example.data.model.Labour
import com.example.data.model.Attendance
import com.example.data.model.AdvancePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface LabourDao {
    
    // --- Labour Queries ---
    @Query("SELECT * FROM labours ORDER BY name ASC")
    fun getAllLabours(): Flow<List<Labour>>

    @Query("SELECT * FROM labours WHERE id = :id LIMIT 1")
    fun getLabourById(id: Long): Flow<Labour?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabour(labour: Labour): Long

    @Update
    suspend fun updateLabour(labour: Labour)

    @Delete
    suspend fun deleteLabour(labour: Labour)

    // --- Attendance Queries ---
    @Query("SELECT * FROM attendance WHERE labourId = :labourId ORDER BY date DESC")
    fun getAttendanceForLabour(labourId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE labourId = :labourId AND date = :date")
    suspend fun deleteAttendanceForDay(labourId: Long, date: String)

    @Query("DELETE FROM attendance WHERE labourId = :labourId")
    suspend fun clearAllAttendanceForLabour(labourId: Long)

    // --- Advance Payment Queries ---
    @Query("SELECT * FROM advance_payments WHERE labourId = :labourId ORDER BY date DESC")
    fun getAdvancesForLabour(labourId: Long): Flow<List<AdvancePayment>>

    @Query("SELECT * FROM advance_payments ORDER BY date DESC")
    fun getAllAdvances(): Flow<List<AdvancePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: AdvancePayment)

    @Query("DELETE FROM advance_payments WHERE id = :id")
    suspend fun deleteAdvanceById(id: Long)

    @Query("DELETE FROM advance_payments WHERE labourId = :labourId")
    suspend fun clearAllAdvancesForLabour(labourId: Long)
}
