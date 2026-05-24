package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Labour
import com.example.data.model.Attendance
import com.example.data.model.AdvancePayment
import com.example.data.repository.LabourRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LabourViewModel(private val repository: LabourRepository) : ViewModel() {

    // Language configuration: true = English, false = Hindi
    private val _isEnglish = MutableStateFlow(true)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    // Navigation and Detail Screen State
    private val _selectedLabourId = MutableStateFlow<Long?>(null)
    val selectedLabourId: StateFlow<Long?> = _selectedLabourId.asStateFlow()

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Database Flows
    val allLabours: StateFlow<List<Labour>> = repository.allLabours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdvances: StateFlow<List<AdvancePayment>> = repository.allAdvances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Labours based on search query
    val filteredLabours: StateFlow<List<Labour>> = combine(allLabours, _searchQuery) { labours, query ->
        if (query.isBlank()) {
            labours
        } else {
            labours.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }

    fun setLanguage(isEng: Boolean) {
        _isEnglish.value = isEng
    }

    fun selectLabour(id: Long?) {
        _selectedLabourId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Worker DB Actions ---

    fun addLabour(name: String, dailyWage: Double, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val labour = Labour(name = name, dailyWage = dailyWage)
            repository.insertLabour(labour)
            onSuccess()
        }
    }

    fun updateLabour(labourId: Long, name: String, dailyWage: Double) {
        viewModelScope.launch {
            val updated = Labour(id = labourId, name = name, dailyWage = dailyWage)
            repository.updateLabour(updated)
        }
    }

    fun deleteLabour(labour: Labour, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteLabourWithData(labour)
            if (_selectedLabourId.value == labour.id) {
                _selectedLabourId.value = null
            }
            onDeleted()
        }
    }

    // --- Attendance DB Actions ---

    fun recordAttendance(labourId: Long, date: String, status: String) {
        viewModelScope.launch {
            repository.setAttendance(labourId, date, status)
        }
    }

    fun cycleAttendance(labourId: Long, date: String, currentStatus: String?) {
        viewModelScope.launch {
            when (currentStatus) {
                null -> repository.setAttendance(labourId, date, "PRESENT")
                "PRESENT" -> repository.setAttendance(labourId, date, "ABSENT")
                "ABSENT" -> repository.deleteAttendance(labourId, date)
            }
        }
    }

    fun deleteAttendance(labourId: Long, date: String) {
        viewModelScope.launch {
            repository.deleteAttendance(labourId, date)
        }
    }

    // --- Advance Payment DB Actions ---

    fun addAdvance(labourId: Long, amount: Double, date: String, note: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addAdvancePayment(labourId, amount, date, note)
            onSuccess()
        }
    }

    fun deleteAdvance(advanceId: Long) {
        viewModelScope.launch {
            repository.deleteAdvancePayment(advanceId)
        }
    }
}

class LabourViewModelFactory(private val repository: LabourRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabourViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabourViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
