package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "labours")
data class Labour(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dailyWage: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["labourId", "date"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val labourId: Long,
    val date: String, // Format: YYYY-MM-DD
    val status: String // "PRESENT" or "ABSENT"
)

@Entity(tableName = "advance_payments")
data class AdvancePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val labourId: Long,
    val amount: Double,
    val date: String, // Format: YYYY-MM-DD
    val note: String = ""
)
