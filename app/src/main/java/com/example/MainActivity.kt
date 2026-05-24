package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.LabourDatabase
import com.example.data.model.Labour
import com.example.data.model.Attendance
import com.example.data.model.AdvancePayment
import com.example.data.repository.LabourRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSubtle
import com.example.ui.translation.AppText
import com.example.ui.translation.get
import com.example.ui.viewmodel.LabourViewModel
import com.example.ui.viewmodel.LabourViewModelFactory
import com.example.utils.DateUtils
import com.example.utils.ReportExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize local Room DB
        val database = LabourDatabase.getDatabase(this)
        val repository = LabourRepository(database.labourDao())
        
        setContent {
            val viewModel: LabourViewModel = viewModel(
                factory = LabourViewModelFactory(repository)
            )
            MyApplicationTheme {
                LabourHisabKitabApp(viewModel)
            }
        }
    }
}

@Composable
fun LabourHisabKitabApp(viewModel: LabourViewModel) {
    val selectedLabourId by viewModel.selectedLabourId.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(
                targetState = selectedLabourId,
                label = "ScreenTransition"
            ) { id ->
                if (id == null) {
                    HomeScreen(
                        viewModel = viewModel,
                        isEnglish = isEnglish
                    )
                } else {
                    DetailScreen(
                        labourId = id,
                        viewModel = viewModel,
                        isEnglish = isEnglish,
                        onBack = {
                            viewModel.selectLabour(null)
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// HOME SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LabourViewModel,
    isEnglish: Boolean
) {
    val labours by viewModel.filteredLabours.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val allAdvances by viewModel.allAdvances.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showAddDialog by varOf(false)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // App Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = AppText.APP_NAME.get(isEnglish),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AppText.SUBTITLE.get(isEnglish),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Language Switch Switcher Button (Large & Clean)
            Button(
                onClick = { viewModel.toggleLanguage() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("language_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Language",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEnglish) "हिन्दी" else "English",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Search Block
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text(AppText.SEARCH_PLACEHOLDER.get(isEnglish)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("search_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
            singleLine = true
        )

        // General Ledger Liability Stats Card
        if (labours.isNotEmpty()) {
            StatsCard(
                labours = labours,
                allAttendance = allAttendance,
                allAdvances = allAdvances,
                isEnglish = isEnglish
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${AppText.TOTAL_LABOURS.get(isEnglish)} (${labours.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Worker scrolling List
        if (labours.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SupervisorAccount,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = AppText.NO_LABOURS_YET.get(isEnglish),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(labours, key = { it.id }) { worker ->
                    // Calculate details for this worker
                    val workerAttendance = allAttendance.filter { it.labourId == worker.id }
                    val workedDaysCount = workerAttendance.count { it.status == "PRESENT" }
                    val totalWageEarned = workedDaysCount * worker.dailyWage
                    val workerAdvances = allAdvances.filter { it.labourId == worker.id }
                    val totalAdvancePaid = workerAdvances.sumOf { it.amount }
                    val dueBalance = totalWageEarned - totalAdvancePaid

                    LabourCardItem(
                        worker = worker,
                        workedDays = workedDaysCount,
                        dueBalance = dueBalance,
                        isEnglish = isEnglish,
                        onClick = { viewModel.selectLabour(worker.id) }
                    )
                }
            }
        }
    }

    // Fabricated FAB (Large & Easy to press!)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .size(64.dp)
                .testTag("add_worker_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = AppText.ADD_LABOUR.get(isEnglish),
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // Add Labour dialog popup
    if (showAddDialog) {
        AddLabourDialog(
            isEnglish = isEnglish,
            onDismiss = { showAddDialog = false },
            onSave = { name, wage ->
                viewModel.addLabour(name, wage) {
                    Toast.makeText(context, AppText.LABOUR_ADDED.get(isEnglish), Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }
}

// ==========================================
// WORKER PROFILE DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    labourId: Long,
    viewModel: LabourViewModel,
    isEnglish: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val labours by viewModel.allLabours.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.allAttendance.collectAsStateWithLifecycle()
    val advanceRecords by viewModel.allAdvances.collectAsStateWithLifecycle()

    val currentWorker = labours.find { it.id == labourId }

    if (currentWorker == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Calculate worker aggregates
    val workerAttendance = attendanceRecords.filter { it.labourId == currentWorker.id }
    val workedDaysCount = workerAttendance.count { it.status == "PRESENT" }
    val totalWageEarned = workedDaysCount * currentWorker.dailyWage

    val workerAdvances = advanceRecords.filter { it.labourId == currentWorker.id }
    val totalAdvancePaid = workerAdvances.sumOf { it.amount }
    
    val remainingBalance = totalWageEarned - totalAdvancePaid

    var showAdvanceDialog by varOf(false)
    var showEditDialog by varOf(false)
    var showDeleteConfirm by varOf(false)

    // Merge attendance & advances into unified chronological timeline
    val todayDateStr = DateUtils.getTodayDateString()
    val todayAtt = workerAttendance.find { it.date == todayDateStr }?.status

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Simple Top App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = currentWorker.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Edit Profile Button
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = AppText.EDIT_LABOUR.get(isEnglish),
                    tint = Color.White
                )
            }

            // Trash can delete button
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = AppText.DELETE.get(isEnglish),
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // METRIC 1: Wage Rate & Statistics Cards
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Current Wage Badge Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppText.WAGE_RATE.get(isEnglish),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "₹ ${currentWorker.dailyWage.toInt()} / ${if (isEnglish) "day" else "दिन"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Grid metric rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Salary Earned
                        MetricCard(
                            title = AppText.TOTAL_SALARY.get(isEnglish),
                            value = "₹ ${totalWageEarned.toInt()}",
                            subtext = "$workedDaysCount ${if (isEnglish) "worked days" else "दिन काम किया"}",
                            icon = Icons.Default.Payments,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )

                        // Total Advance Paid
                        MetricCard(
                            title = AppText.TOTAL_ADVANCE.get(isEnglish),
                            value = "₹ ${totalAdvancePaid.toInt()}",
                            subtext = "${workerAdvances.size} ${if (isEnglish) "payments" else "पेमेंट"}",
                            icon = Icons.Default.AccountBalanceWallet,
                            color = Color(0xFFD84315),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Balance Card (Big Contrast Area)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (remainingBalance >= 0) Color(0xFFE3F2FD) else Color(0xFFFFEBEE)
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (remainingBalance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppText.REMAINING_BALANCE.get(isEnglish),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹ ${remainingBalance.toInt()}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (remainingBalance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F),
                                modifier = Modifier.testTag("remaining_balance_value")
                            )
                        }
                    }
                }
            }

            // ACTION CONTROLS: Attendance Toggles (Today's check!)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppText.TODAY_ATTENDANCE.get(isEnglish),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Large Attendance Setting Option Pair
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Present Button (Large Green Design)
                            Button(
                                onClick = { 
                                    viewModel.recordAttendance(currentWorker.id, todayDateStr, "PRESENT")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("today_present_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (todayAtt == "PRESENT") Color(0xFF4CAF50) else Color(0xFFE8F5E9),
                                    contentColor = if (todayAtt == "PRESENT") Color.White else Color(0xFF2E7D32)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEnglish) "PRESENT" else "हाजिर (P)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // Absent Button (Large Red Design)
                            Button(
                                onClick = { 
                                    viewModel.recordAttendance(currentWorker.id, todayDateStr, "ABSENT")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("today_absent_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (todayAtt == "ABSENT") Color(0xFFD32F2F) else Color(0xFFFFEBEE),
                                    contentColor = if (todayAtt == "ABSENT") Color.White else Color(0xFFC62828)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEnglish) "ABSENT" else "गैरहाजिर (A)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        if (todayAtt != null) {
                            TextButton(
                                onClick = { viewModel.deleteAttendance(currentWorker.id, todayDateStr) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppText.CLEAR_RECORD.get(isEnglish), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // CORE SHORTCUT CONTROL: cash advances & exports
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Open Advance Payment dialog trigger
                    Button(
                        onClick = { showAdvanceDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalActivity, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppText.ADD_ADVANCE.get(isEnglish), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Share Ledger Reports
                    Button(
                        onClick = { 
                            ReportExporter.shareLabourLedger(
                                context = context,
                                labour = currentWorker,
                                attendanceList = workerAttendance,
                                advanceList = workerAdvances,
                                isEnglish = isEnglish
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00796B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppText.EXPORT_REPORT.get(isEnglish), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // LEDGER HISTORICAL LISTING
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppText.HISTORY.get(isEnglish),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Consolidated items listing
            val timelineItems = assembleWorkerTimeline(workerAttendance, workerAdvances)
            if (timelineItems.isEmpty()) {
                item {
                    Text(
                        text = if (isEnglish) "No history records found for this labour." else "इस लेबर के लिए इतिहास का कोई रिकॉर्ड नहीं मिला है।",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                items(timelineItems) { item ->
                    TimelineItemRow(
                        item = item,
                        isEnglish = isEnglish,
                        onDeleteAdvance = { advId ->
                            viewModel.deleteAdvance(advId)
                        },
                        onToggleAttendance = { att ->
                            viewModel.cycleAttendance(att.labourId, att.date, att.status)
                        }
                    )
                }
            }
        }
    }

    // Give Advance Payment Dialog Popup Helper
    if (showAdvanceDialog) {
        AddAdvanceDialog(
            isEnglish = isEnglish,
            onDismiss = { showAdvanceDialog = false },
            onSave = { amount, note ->
                viewModel.addAdvance(currentWorker.id, amount, DateUtils.getTodayDateString(), note) {
                    Toast.makeText(context, AppText.ADVANCE_ADDED.get(isEnglish), Toast.LENGTH_SHORT).show()
                }
                showAdvanceDialog = false
            }
        )
    }

    // Edit Labour Profile Wage Settings Dialog
    if (showEditDialog) {
        EditLabourDialog(
            labour = currentWorker,
            isEnglish = isEnglish,
            onDismiss = { showEditDialog = false },
            onSave = { name, wage ->
                viewModel.updateLabour(currentWorker.id, name, wage)
                showEditDialog = false
            }
        )
    }

    // Confirm Deletion Alert
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(AppText.DELETE.get(isEnglish)) },
            text = { Text(AppText.CONFIRM_DELETE.get(isEnglish)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLabour(currentWorker) {
                            Toast.makeText(context, AppText.DELETE_SUCCESS.get(isEnglish), Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(AppText.DELETE.get(isEnglish), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(AppText.CANCEL.get(isEnglish))
                }
            }
        )
    }
}

// ==========================================
// INDIVIDUAL COMPOSABLE COMPONENTS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourCardItem(
    worker: Labour,
    workedDays: Int,
    dueBalance: Double,
    isEnglish: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("labour_worker_card_${worker.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First Letter of Name Avatar Circular Layout
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = worker.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Body Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₹ ${worker.dailyWage.toInt()} / ${if (isEnglish) "day" else "दिन"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtle
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Number of days worked summary
                Text(
                    text = if (isEnglish) "$workedDays Present Days" else "$workedDays दिन हाजिर",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Price/Ledger metrics align
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = AppText.REMAINING_BALANCE.get(isEnglish),
                    fontSize = 10.sp,
                    color = TextSubtle,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹ ${dueBalance.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (dueBalance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    labours: List<Labour>,
    allAttendance: List<Attendance>,
    allAdvances: List<AdvancePayment>,
    isEnglish: Boolean
) {
    // Collect stats total on all items
    val totalWorkers = labours.size
    var totalLiability = 0.0

    labours.forEach { worker ->
        val wkAtt = allAttendance.filter { it.labourId == worker.id }
        val presentNum = wkAtt.count { it.status == "PRESENT" }
        val sumEarned = presentNum * worker.dailyWage
        val sumAdvance = allAdvances.filter { it.labourId == worker.id }.sumOf { it.amount }
        totalLiability += (sumEarned - sumAdvance)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TOTAL LIABILITY / कुल बकाया राशि",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹ ${totalLiability.toInt()}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isEnglish) "Active Labours" else "सक्रिय लेबर",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = "$totalWorkers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSubtle,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Timeline Helper Structures and Composable Screen Layout
sealed class TimelineItem(val dateString: String) {
    class AttendItem(val attendance: Attendance) : TimelineItem(attendance.date)
    class CashAdvance(val advance: AdvancePayment) : TimelineItem(advance.date)
}

fun assembleWorkerTimeline(attendance: List<Attendance>, advances: List<AdvancePayment>): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    attendance.forEach { items.add(TimelineItem.AttendItem(it)) }
    advances.forEach { items.add(TimelineItem.CashAdvance(it)) }
    return items.sortedByDescending { it.dateString }
}

@Composable
fun TimelineItemRow(
    item: TimelineItem,
    isEnglish: Boolean,
    onDeleteAdvance: (Long) -> Unit,
    onToggleAttendance: (Attendance) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle Status Icon Accent
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (item) {
                                is TimelineItem.AttendItem -> {
                                    if (item.attendance.status == "PRESENT") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                }
                                is TimelineItem.CashAdvance -> Color(0xFFFFF3E0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val vectorIcon = when (item) {
                        is TimelineItem.AttendItem -> {
                            if (item.attendance.status == "PRESENT") Icons.Default.CheckCircle else Icons.Default.Cancel
                        }
                        is TimelineItem.CashAdvance -> Icons.Default.LocalAtm
                    }
                    val iconTint = when (item) {
                        is TimelineItem.AttendItem -> {
                            if (item.attendance.status == "PRESENT") Color(0xFF4CAF50) else Color(0xFFD32F2F)
                        }
                        is TimelineItem.CashAdvance -> Color(0xFFF57C00)
                    }
                    Icon(imageVector = vectorIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Detail Column titles
                Column {
                    Text(
                        text = DateUtils.formatDateForDisplay(item.dateString, isEnglish),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (item) {
                            is TimelineItem.AttendItem -> {
                                if (item.attendance.status == "PRESENT") AppText.PRESENT.get(isEnglish) else AppText.ABSENT.get(isEnglish)
                            }
                            is TimelineItem.CashAdvance -> {
                                val baseNote = if (item.advance.note.isNotBlank()) " (${item.advance.note})" else ""
                                "${AppText.TOTAL_ADVANCE.get(isEnglish)}$baseNote"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }
            }

            // Value indicators or quick deletion row
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (item) {
                    is TimelineItem.AttendItem -> {
                        // Double Tap to Cycle Attendance helper
                        IconButton(onClick = { onToggleAttendance(item.attendance) }) {
                            Icon(Icons.Default.SyncAlt, contentDescription = "Cycle Status", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    is TimelineItem.CashAdvance -> {
                        Text(
                            text = "- ₹ ${item.advance.amount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD84315),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { onDeleteAdvance(item.advance.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// Add Labour Dialog
@Composable
fun AddLabourDialog(
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var name by varOf("")
    var wageStr by varOf("")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = AppText.ADD_LABOUR.get(isEnglish),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Worker Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppText.LABOUR_NAME.get(isEnglish)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_worker_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Daily Wage Rate Field
                OutlinedTextField(
                    value = wageStr,
                    onValueChange = { wageStr = it },
                    label = { Text(AppText.DAILY_WAGE.get(isEnglish)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_worker_wage_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Actions Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppText.CANCEL.get(isEnglish))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedWage = wageStr.toDoubleOrNull()
                            if (name.isNotBlank() && parsedWage != null && parsedWage > 0) {
                                onSave(name.trim(), parsedWage)
                            }
                        },
                        modifier = Modifier.testTag("add_worker_save_btn"),
                        enabled = name.isNotBlank() && wageStr.isNotBlank()
                    ) {
                        Text(AppText.SAVE.get(isEnglish))
                    }
                }
            }
        }
    }
}

// Edit Worker Info Dialog
@Composable
fun EditLabourDialog(
    labour: Labour,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var name by varOf(labour.name)
    var wageStr by varOf(labour.dailyWage.toInt().toString())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = AppText.EDIT_LABOUR.get(isEnglish),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppText.LABOUR_NAME.get(isEnglish)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = wageStr,
                    onValueChange = { wageStr = it },
                    label = { Text(AppText.DAILY_WAGE.get(isEnglish)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppText.CANCEL.get(isEnglish))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedWage = wageStr.toDoubleOrNull()
                            if (name.isNotBlank() && parsedWage != null && parsedWage > 0) {
                                onSave(name.trim(), parsedWage)
                            }
                        },
                        enabled = name.isNotBlank() && wageStr.isNotBlank()
                    ) {
                        Text(AppText.SAVE.get(isEnglish))
                    }
                }
            }
        }
    }
}

// Give Cash Advance Dialog
@Composable
fun AddAdvanceDialog(
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    var amountStr by varOf("")
    var noteStr by varOf("")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = AppText.ADD_ADVANCE.get(isEnglish),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(AppText.ADVANCE_AMOUNT.get(isEnglish)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text(AppText.NOTE_OPTIONAL.get(isEnglish)) },
                    placeholder = { Text(AppText.REASON_HINT.get(isEnglish)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_note_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppText.CANCEL.get(isEnglish))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmt = amountStr.toDoubleOrNull()
                            if (parsedAmt != null && parsedAmt > 0) {
                                onSave(parsedAmt, noteStr.trim())
                            }
                        },
                        modifier = Modifier.testTag("advance_save_btn"),
                        enabled = amountStr.isNotBlank()
                    ) {
                        Text(AppText.SAVE.get(isEnglish))
                    }
                }
            }
        }
    }
}

// Helper shorthand for state creation inside Composable functions
@Composable
fun <T> varOf(value: T): MutableState<T> {
    return remember { mutableStateOf(value) }
}
