package com.example.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Labour
import com.example.data.model.Attendance
import com.example.data.model.AdvancePayment
import com.example.ui.translation.AppText
import com.example.ui.translation.get
import java.io.File
import java.io.FileWriter

object ReportExporter {

    fun shareLabourLedger(
        context: Context,
        labour: Labour,
        attendanceList: List<Attendance>,
        advanceList: List<AdvancePayment>,
        isEnglish: Boolean
    ) {
        try {
            // Calculations
            val workedDays = attendanceList.count { it.status == "PRESENT" }
            val totalSalary = workedDays * labour.dailyWage
            val totalAdvance = advanceList.sumOf { it.amount }
            val balance = totalSalary - totalAdvance

            // Generate beautifully formatted CSV
            val csvContent = StringBuilder()

            // Header info
            csvContent.append("\"${AppText.APP_NAME.get(isEnglish)} - ${labour.name} Ledger\"\n")
            csvContent.append("\"${AppText.LABOUR_NAME.get(isEnglish)}:\",\"${labour.name}\"\n")
            csvContent.append("\"${AppText.WAGE_RATE.get(isEnglish)}:\",\"Rs. ${labour.dailyWage}\"\n")
            csvContent.append("\"${AppText.PRESENT_DAYS.get(isEnglish)}:\",\"$workedDays\"\n")
            csvContent.append("\"${AppText.TOTAL_SALARY.get(isEnglish)}:\",\"Rs. $totalSalary\"\n")
            csvContent.append("\"${AppText.ADVANCE_PAID.get(isEnglish)}:\",\"Rs. $totalAdvance\"\n")
            csvContent.append("\"${AppText.REMAINING_BALANCE.get(isEnglish)}:\",\"Rs. $balance\"\n\n")

            // Attendance Section
            csvContent.append("\"--- ${AppText.ATTENDANCE.get(isEnglish)} ${AppText.HISTORY.get(isEnglish)} ---\"\n")
            csvContent.append("\"${AppText.DATE.get(isEnglish)}\",\"${AppText.ATTENDANCE.get(isEnglish)}\"\n")
            if (attendanceList.isEmpty()) {
                csvContent.append("\"No Records\",\"\"\n")
            } else {
                attendanceList.sortedByDescending { it.date }.forEach {
                    val statusText = if (it.status == "PRESENT") AppText.PRESENT.get(isEnglish) else AppText.ABSENT.get(isEnglish)
                    val dateFormatted = DateUtils.formatDateForDisplay(it.date, isEnglish)
                    csvContent.append("\"$dateFormatted\",\"$statusText\"\n")
                }
            }
            csvContent.append("\n")

            // Advance Ledger Section
            csvContent.append("\"--- ${AppText.TOTAL_ADVANCE.get(isEnglish)} ${AppText.HISTORY.get(isEnglish)} ---\"\n")
            csvContent.append("\"${AppText.DATE.get(isEnglish)}\",\"${AppText.ADVANCE_AMOUNT.get(isEnglish)}\",\"${AppText.NOTE_OPTIONAL.get(isEnglish)}\"\n")
            if (advanceList.isEmpty()) {
                csvContent.append("\"No Records\",\"\",\"\"\n")
            } else {
                advanceList.sortedByDescending { it.date }.forEach {
                    val dateFormatted = DateUtils.formatDateForDisplay(it.date, isEnglish)
                    csvContent.append("\"$dateFormatted\",\"Rs. ${it.amount}\",\"${it.note}\"\n")
                }
            }

            // Create temporary cache file
            val cachePath = File(context.cacheDir, "shared_reports")
            cachePath.mkdirs()
            
            // Clean file name
            val safeName = labour.name.replace("\\s+".toRegex(), "_")
            val file = File(cachePath, "Labour_Report_${safeName}.csv")
            
            val writer = FileWriter(file)
            writer.write(csvContent.toString())
            writer.flush()
            writer.close()

            // Get standard content Uri
            val authority = "com.aistudio.labourhisab.khbjsy.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            // Setup share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "${labour.name} - ${AppText.APP_NAME.get(isEnglish)}")
                putExtra(Intent.EXTRA_TEXT, "${AppText.REPORT_BODY.get(isEnglish)}\n\n${labour.name}\n${AppText.REMAINING_BALANCE.get(isEnglish)}: Rs. $balance")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Start chooser
            val chooser = Intent.createChooser(shareIntent, "${AppText.EXPORT_REPORT.get(isEnglish)}: ${labour.name}")
            // Required for SDK 24+ file reading permissions
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
