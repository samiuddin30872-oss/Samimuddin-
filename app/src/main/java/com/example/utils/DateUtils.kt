package com.example.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DB_DATE_FORMAT = "yyyy-MM-dd"

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat(DB_DATE_FORMAT, Locale.US)
        return sdf.format(Date())
    }

    fun formatDateForDisplay(dateStr: String, isEnglish: Boolean): String {
        return try {
            val dbSdf = SimpleDateFormat(DB_DATE_FORMAT, Locale.US)
            val date = dbSdf.parse(dateStr) ?: return dateStr
            
            if (isEnglish) {
                val displaySdf = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
                displaySdf.format(date)
            } else {
                // Formatting format fits standard readable translation
                val displaySdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
                val engDate = displaySdf.format(date)
                // Let's customize key months for clean Hindi display
                engDate
                    .replace("Jan", "जनवरी")
                    .replace("Feb", "फरवरी")
                    .replace("Mar", "मार्च")
                    .replace("Apr", "अप्रैल")
                    .replace("May", "मई")
                    .replace("Jun", "जून")
                    .replace("Jul", "जुलाई")
                    .replace("Aug", "अगस्त")
                    .replace("Sep", "सितंबर")
                    .replace("Oct", "अक्टूबर")
                    .replace("Nov", "नवंबर")
                    .replace("Dec", "दिसंबर")
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getLast30Days(): List<String> {
        val dateList = mutableListOf<String>()
        val sdf = SimpleDateFormat(DB_DATE_FORMAT, Locale.US)
        val calendar = Calendar.getInstance()
        
        for (i in 0 until 30) {
            dateList.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return dateList
    }

    fun getFormattedMonthYear(dateStr: String): String {
        return try {
            val dbSdf = SimpleDateFormat(DB_DATE_FORMAT, Locale.US)
            val date = dbSdf.parse(dateStr) ?: return ""
            val monthSdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            monthSdf.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}
