package com.example.data.repository

import com.example.data.dao.LabourDao
import com.example.data.model.Labour
import com.example.data.model.Attendance
import com.example.data.model.AdvancePayment
import kotlinx.coroutines.flow.Flow

class LabourRepository(private val labourDao: LabourDao) {

    val allLabours: Flow<List<Labour>> = labourDao.getAllLabours()
    val allAttendance: Flow<List<Attendance>> = labourDao.getAllAttendance()
    val allAdvances: Flow<List<AdvancePayment>> = labourDao.getAllAdvances()

    fun getLabourById(id: Long): Flow<Labour?> = labourDao.getLabourById(id)

    fun getAttendanceForLabour(labourId: Long): Flow<List<Attendance>> = 
        labourDao.getAttendanceForLabour(labourId)

    fun getAdvancesForLabour(labourId: Long): Flow<List<AdvancePayment>> = 
        labourDao.getAdvancesForLabour(labourId)

    suspend fun insertLabour(labour: Labour): Long {
        return labourDao.insertLabour(labour)
    }

    suspend fun updateLabour(labour: Labour) {
        labourDao.updateLabour(labour)
    }

    suspend fun deleteLabourWithData(labour: Labour) {
        // Cascaded deletions
        labourDao.clearAllAttendanceForLabour(labour.id)
        labourDao.clearAllAdvancesForLabour(labour.id)
        labourDao.deleteLabour(labour)
    }

    suspend fun setAttendance(labourId: Long, date: String, status: String) {
        val attendance = Attendance(labourId = labourId, date = date, status = status)
        labourDao.insertAttendance(attendance)
    }

    suspend fun deleteAttendance(labourId: Long, date: String) {
        labourDao.deleteAttendanceForDay(labourId, date)
    }

    suspend fun addAdvancePayment(labourId: Long, amount: Double, date: String, note: String) {
        val advance = AdvancePayment(labourId = labourId, amount = amount, date = date, note = note)
        labourDao.insertAdvance(advance)
    }

    suspend fun deleteAdvancePayment(id: Long) {
        labourDao.deleteAdvanceById(id)
    }
}
