package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.FirestoreBankInstallment
import com.example.data.firestore.FirestoreDailyLog
import com.example.data.firestore.FirestoreDriverPayout
import com.example.data.firestore.FirestoreRepository
import com.example.data.firestore.FirestoreVehicle
import com.example.data.model.SalaryType
import com.example.ui.auth.AuthManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import com.example.data.ai.GeminiFleetService
import com.example.data.ai.ParsedSmartEntry
import com.example.ui.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySummary(
    val date: String,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalFuelExpense: Double = 0.0,
    val totalEvChargingExpense: Double = 0.0,
    val totalDriverWage: Double = 0.0,
    val netDailyProfit: Double = 0.0,
    val activeVehiclesCount: Int = 0,
    val logsCount: Int = 0
)

data class VehicleMonthlyStat(
    val vehicle: FirestoreVehicle,
    val grossIncome: Double = 0.0,
    val fuelExpense: Double = 0.0,
    val evChargingExpense: Double = 0.0,
    val maintenanceExpense: Double = 0.0,
    val otherExpenses: Double = 0.0,
    val totalOperatingExpenses: Double = 0.0,
    val driverWageEarned: Double = 0.0,
    val operatingNet: Double = 0.0,
    val bankEmiPaid: Double = 0.0,
    val netVehicleProfit: Double = 0.0,
    val daysOperated: Int = 0
)

data class MonthlyReport(
    val yearMonth: String,
    val totalIncome: Double = 0.0,
    val totalFuel: Double = 0.0,
    val totalEvCharging: Double = 0.0,
    val totalToll: Double = 0.0,
    val totalMaintenance: Double = 0.0,
    val totalOtherExpenses: Double = 0.0,
    val totalOperatingExpenses: Double = 0.0,
    val totalDriverWages: Double = 0.0,
    val totalBankEmiPaid: Double = 0.0,
    val totalBankEmiDue: Double = 0.0,
    val netFleetProfit: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val vehicleStats: List<VehicleMonthlyStat> = emptyList()
)

data class DriverDailyRecord(
    val date: String,
    val vehicleReg: String,
    val wageEarned: Double,
    val amountPaid: Double,
    val unpaidDailyBalance: Double,
    val notes: String
)

data class DriverSummary(
    val driverName: String,
    val vehicleId: String,
    val vehicleReg: String,
    val phone: String,
    val totalWageEarned: Double,
    val totalPaidViaLogs: Double,
    val totalAdditionalPayouts: Double,
    val totalPaidOverall: Double,
    val pendingBalance: Double,
    val dailyRecords: List<DriverDailyRecord> = emptyList()
)

class FleetViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo: FirestoreRepository = FirestoreRepository.create(application)
    private val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _selectedDate = MutableStateFlow(sdfDate.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(sdfMonth.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedVehicleFilter = MutableStateFlow<String?>(null) // null = all
    val selectedVehicleFilter: StateFlow<String?> = _selectedVehicleFilter.asStateFlow()

    private val _currency = MutableStateFlow("Rs.") // default Rs. (can be $, ₹, etc)
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.NEPALI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isFatherMode = MutableStateFlow(true) // Default to elderly friendly mode!
    val isFatherMode: StateFlow<Boolean> = _isFatherMode.asStateFlow()

    private val _isUpdateAvailable = MutableStateFlow(true) // Indicates version 1.1 update available
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _showDownloadShareDialog = MutableStateFlow(false)
    val showDownloadShareDialog: StateFlow<Boolean> = _showDownloadShareDialog.asStateFlow()

    private val geminiService = GeminiFleetService()

    // AI Advisor State
    private val _showAiAdvisorDialog = MutableStateFlow(false)
    val showAiAdvisorDialog: StateFlow<Boolean> = _showAiAdvisorDialog.asStateFlow()

    private val _aiAdvisorQuestion = MutableStateFlow("")
    val aiAdvisorQuestion: StateFlow<String> = _aiAdvisorQuestion.asStateFlow()

    private val _aiAdvisorResponse = MutableStateFlow("")
    val aiAdvisorResponse: StateFlow<String> = _aiAdvisorResponse.asStateFlow()

    private val _isAiAdvisorLoading = MutableStateFlow(false)
    val isAiAdvisorLoading: StateFlow<Boolean> = _isAiAdvisorLoading.asStateFlow()

    private val _aiAdvisorError = MutableStateFlow<String?>(null)
    val aiAdvisorError: StateFlow<String?> = _aiAdvisorError.asStateFlow()

    // AI Smart Voice / Text Entry State
    private val _showSmartVoiceEntryDialog = MutableStateFlow(false)
    val showSmartVoiceEntryDialog: StateFlow<Boolean> = _showSmartVoiceEntryDialog.asStateFlow()

    private val _smartVoiceInputText = MutableStateFlow("")
    val smartVoiceInputText: StateFlow<String> = _smartVoiceInputText.asStateFlow()

    private val _isSmartParsing = MutableStateFlow(false)
    val isSmartParsing: StateFlow<Boolean> = _isSmartParsing.asStateFlow()

    private val _parsedSmartEntry = MutableStateFlow<ParsedSmartEntry?>(null)
    val parsedSmartEntry: StateFlow<ParsedSmartEntry?> = _parsedSmartEntry.asStateFlow()

    private val _smartParseError = MutableStateFlow<String?>(null)
    val smartParseError: StateFlow<String?> = _smartParseError.asStateFlow()

    // Monthly AI Insight State
    private val _monthlyAiInsight = MutableStateFlow<String?>(null)
    val monthlyAiInsight: StateFlow<String?> = _monthlyAiInsight.asStateFlow()

    private val _isGeneratingMonthlyInsight = MutableStateFlow(false)
    val isGeneratingMonthlyInsight: StateFlow<Boolean> = _isGeneratingMonthlyInsight.asStateFlow()

    private val _monthlyInsightError = MutableStateFlow<String?>(null)
    val monthlyInsightError: StateFlow<String?> = _monthlyInsightError.asStateFlow()

    private val _localVehicles = MutableStateFlow<List<FirestoreVehicle>>(getSeedVehicles())
    private val _localDailyLogs = MutableStateFlow<List<FirestoreDailyLog>>(getSeedDailyLogs())
    private val _localDriverPayouts = MutableStateFlow<List<FirestoreDriverPayout>>(getSeedDriverPayouts())
    private val _localInstallments = MutableStateFlow<List<FirestoreBankInstallment>>(getSeedInstallments())

    init {
        // Observe Auth State
        Firebase.auth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            if (auth.currentUser != null) {
                viewModelScope.launch {
                    try {
                        firestoreRepo.seedInitialUserDataIfEmpty()
                        syncLocalDataToFirestore()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // Real-time Cloud Firestore observations when authenticated, or reactive local storage when offline/guest
    val allVehicles: StateFlow<List<FirestoreVehicle>> = _currentUser.flatMapLatest { user ->
        if (user != null) firestoreRepo.observeVehicles() else _localVehicles
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getSeedVehicles())

    val allDailyLogs: StateFlow<List<FirestoreDailyLog>> = _currentUser.flatMapLatest { user ->
        if (user != null) firestoreRepo.observeDailyLogs() else _localDailyLogs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getSeedDailyLogs())

    val allDriverPayouts: StateFlow<List<FirestoreDriverPayout>> = _currentUser.flatMapLatest { user ->
        if (user != null) firestoreRepo.observeDriverPayouts() else _localDriverPayouts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getSeedDriverPayouts())

    val allInstallments: StateFlow<List<FirestoreBankInstallment>> = _currentUser.flatMapLatest { user ->
        if (user != null) firestoreRepo.observeBankInstallments() else _localInstallments
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getSeedInstallments())

    // Daily Summary for selectedDate
    val todaySummary: StateFlow<DailySummary> = combine(
        allDailyLogs,
        allVehicles,
        selectedDate,
        selectedVehicleFilter
    ) { logs, vehicles, date, vehicleFilter ->
        val filteredLogs = logs.filter {
            it.date == date && (vehicleFilter == null || it.vehicleId == vehicleFilter)
        }
        val income = filteredLogs.sumOf { it.grossIncome }
        val fuel = filteredLogs.sumOf { it.fuelExpense }
        val ev = filteredLogs.sumOf { it.evChargingExpense }
        val expenses = filteredLogs.sumOf { it.totalOperatingExpenses }
        val driverWages = filteredLogs.sumOf { it.driverWageEarned }
        val net = filteredLogs.sumOf { it.netDailyProfit }

        DailySummary(
            date = date,
            totalIncome = income,
            totalExpenses = expenses,
            totalFuelExpense = fuel,
            totalEvChargingExpense = ev,
            totalDriverWage = driverWages,
            netDailyProfit = net,
            activeVehiclesCount = vehicles.size,
            logsCount = filteredLogs.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary(date = ""))

    // Monthly Report for selectedMonth
    val monthlyReport: StateFlow<MonthlyReport> = combine(
        allDailyLogs,
        allVehicles,
        allInstallments,
        selectedMonth,
        selectedVehicleFilter
    ) { logs, vehicles, installments, month, vehicleFilter ->
        val monthLogs = logs.filter {
            it.date.startsWith(month) && (vehicleFilter == null || it.vehicleId == vehicleFilter)
        }
        val monthInstallments = installments.filter {
            it.yearMonth == month && (vehicleFilter == null || it.vehicleId == vehicleFilter)
        }

        val totalIncome = monthLogs.sumOf { it.grossIncome }
        val totalFuel = monthLogs.sumOf { it.fuelExpense }
        val totalEv = monthLogs.sumOf { it.evChargingExpense }
        val totalToll = monthLogs.sumOf { it.tollAndTaxExpense }
        val totalMaintenance = monthLogs.sumOf { it.maintenanceExpense }
        val totalOther = monthLogs.sumOf { it.challanOrFineExpense + it.helperWageExpense + it.foodOrMiscExpense }
        val totalOperatingExp = monthLogs.sumOf { it.totalOperatingExpenses }
        val totalDriverWages = monthLogs.sumOf { it.driverWageEarned }
        val totalEmiPaid = monthInstallments.sumOf { it.amountPaid }
        val totalEmiDue = monthInstallments.sumOf { it.amountDue }

        val netFleetProfit = totalIncome - totalOperatingExp - totalDriverWages - totalEmiPaid
        val profitMargin = if (totalIncome > 0) (netFleetProfit / totalIncome) * 100.0 else 0.0

        val relevantVehicles = if (vehicleFilter == null) vehicles else vehicles.filter { it.id == vehicleFilter }
        val vehicleStats = relevantVehicles.map { v ->
            val vLogs = monthLogs.filter { it.vehicleId == v.id }
            val vInstallment = monthInstallments.find { it.vehicleId == v.id }
            val vIncome = vLogs.sumOf { it.grossIncome }
            val vFuel = vLogs.sumOf { it.fuelExpense }
            val vEv = vLogs.sumOf { it.evChargingExpense }
            val vMaint = vLogs.sumOf { it.maintenanceExpense }
            val vOther = vLogs.sumOf { it.tollAndTaxExpense + it.challanOrFineExpense + it.helperWageExpense + it.foodOrMiscExpense }
            val vOpExp = vLogs.sumOf { it.totalOperatingExpenses }
            val vDriverWage = vLogs.sumOf { it.driverWageEarned }
            val vOpNet = vIncome - vOpExp - vDriverWage
            val vEmiPaid = vInstallment?.amountPaid ?: 0.0
            val vNet = vOpNet - vEmiPaid

            VehicleMonthlyStat(
                vehicle = v,
                grossIncome = vIncome,
                fuelExpense = vFuel,
                evChargingExpense = vEv,
                maintenanceExpense = vMaint,
                otherExpenses = vOther,
                totalOperatingExpenses = vOpExp,
                driverWageEarned = vDriverWage,
                operatingNet = vOpNet,
                bankEmiPaid = vEmiPaid,
                netVehicleProfit = vNet,
                daysOperated = vLogs.size
            )
        }

        MonthlyReport(
            yearMonth = month,
            totalIncome = totalIncome,
            totalFuel = totalFuel,
            totalEvCharging = totalEv,
            totalToll = totalToll,
            totalMaintenance = totalMaintenance,
            totalOtherExpenses = totalOther,
            totalOperatingExpenses = totalOperatingExp,
            totalDriverWages = totalDriverWages,
            totalBankEmiPaid = totalEmiPaid,
            totalBankEmiDue = totalEmiDue,
            netFleetProfit = netFleetProfit,
            profitMarginPercent = profitMargin,
            vehicleStats = vehicleStats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyReport(yearMonth = ""))

    // Driver statements with date-by-date breakdown
    val driverSummaries: StateFlow<List<DriverSummary>> = combine(
        allVehicles,
        allDailyLogs,
        allDriverPayouts
    ) { vehicles, logs, payouts ->
        vehicles.map { vehicle ->
            val vLogs = logs.filter { it.vehicleId == vehicle.id }.sortedByDescending { it.date }
            val vPayouts = payouts.filter { it.vehicleId == vehicle.id }

            val totalEarned = vLogs.sumOf { it.driverWageEarned }
            val totalPaidInLogs = vLogs.sumOf { it.driverPaidAmount }
            val totalPayouts = vPayouts.sumOf { it.amount }
            val totalPaid = totalPaidInLogs + totalPayouts
            val pending = totalEarned - totalPaid

            val dailyRecords = vLogs.map { log ->
                val unpaid = (log.driverWageEarned - log.driverPaidAmount).coerceAtLeast(0.0)
                DriverDailyRecord(
                    date = log.date,
                    vehicleReg = vehicle.regNumber,
                    wageEarned = log.driverWageEarned,
                    amountPaid = log.driverPaidAmount,
                    unpaidDailyBalance = unpaid,
                    notes = log.notes
                )
            }

            DriverSummary(
                driverName = vehicle.driverName.ifBlank { "Driver (${vehicle.regNumber})" },
                vehicleId = vehicle.id,
                vehicleReg = vehicle.regNumber,
                phone = vehicle.driverPhone,
                totalWageEarned = totalEarned,
                totalPaidViaLogs = totalPaidInLogs,
                totalAdditionalPayouts = totalPayouts,
                totalPaidOverall = totalPaid,
                pendingBalance = pending,
                dailyRecords = dailyRecords
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
    }

    fun setVehicleFilter(vehicleId: String?) {
        _selectedVehicleFilter.value = vehicleId
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
    }

    fun setLanguage(newLang: AppLanguage) {
        _language.value = newLang
    }

    fun setFatherMode(enabled: Boolean) {
        _isFatherMode.value = enabled
    }

    fun setShowUpdateDialog(show: Boolean) {
        _showUpdateDialog.value = show
    }

    fun setShowDownloadShareDialog(show: Boolean) {
        _showDownloadShareDialog.value = show
    }

    fun syncLocalDataToFirestore() {
        if (_currentUser.value == null) return
        viewModelScope.launch {
            try {
                _localVehicles.value.forEach { v ->
                    firestoreRepo.saveVehicle(v)
                }
                _localDailyLogs.value.forEach { l ->
                    firestoreRepo.saveDailyLog(l)
                }
                _localDriverPayouts.value.forEach { p ->
                    firestoreRepo.recordDriverPayout(p)
                }
                _localInstallments.value.forEach { i ->
                    firestoreRepo.saveBankInstallment(i)
                }
            } catch (_: Exception) {}
        }
    }

    fun calculateDriverWage(
        salaryTypeStr: String,
        salaryRate: Double,
        grossIncome: Double,
        totalExpenses: Double
    ): Double {
        return when (salaryTypeStr) {
            SalaryType.FIXED_DAILY.name -> salaryRate
            SalaryType.PERCENT_OF_INCOME.name -> (grossIncome * salaryRate / 100.0)
            SalaryType.PERCENT_OF_NET.name -> {
                val netBeforeDriver = (grossIncome - totalExpenses).coerceAtLeast(0.0)
                (netBeforeDriver * salaryRate / 100.0)
            }
            else -> salaryRate
        }
    }

    fun saveVehicle(vehicle: FirestoreVehicle) {
        val assignedId = if (vehicle.id.isBlank()) "v_${System.currentTimeMillis()}" else vehicle.id
        val updatedVehicle = vehicle.copy(id = assignedId)
        
        // Update local state immediately
        _localVehicles.value = _localVehicles.value.filter { it.id != assignedId } + updatedVehicle

        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.saveVehicle(updatedVehicle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        _localVehicles.value = _localVehicles.value.filter { it.id != vehicleId }
        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.deleteVehicle(vehicleId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveDailyLog(log: FirestoreDailyLog) {
        val assignedId = if (log.id.isBlank()) "log_${System.currentTimeMillis()}" else log.id
        val updatedLog = log.copy(id = assignedId)
        
        _localDailyLogs.value = _localDailyLogs.value.filter { it.id != assignedId } + updatedLog

        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.saveDailyLog(updatedLog)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteDailyLog(logId: String) {
        _localDailyLogs.value = _localDailyLogs.value.filter { it.id != logId }
        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.deleteDailyLog(logId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun recordDriverPayout(
        vehicleId: String,
        driverName: String,
        amount: Double,
        date: String,
        mode: String,
        notes: String
    ) {
        val v = allVehicles.value.find { it.id == vehicleId }
        val payout = FirestoreDriverPayout(
            id = "payout_${System.currentTimeMillis()}",
            vehicleId = vehicleId,
            vehicleReg = v?.regNumber ?: "",
            driverName = driverName,
            amount = amount,
            date = date,
            paymentMode = mode,
            notes = notes
        )
        _localDriverPayouts.value = _localDriverPayouts.value + payout

        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.recordDriverPayout(payout)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteDriverPayout(payoutId: String) {
        _localDriverPayouts.value = _localDriverPayouts.value.filter { it.id != payoutId }
        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.deleteDriverPayout(payoutId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveBankInstallment(installment: FirestoreBankInstallment) {
        val assignedId = if (installment.id.isBlank()) "emi_${installment.vehicleId}_${installment.yearMonth}" else installment.id
        val updated = installment.copy(id = assignedId)
        _localInstallments.value = _localInstallments.value.filter { it.id != assignedId && !(it.vehicleId == installment.vehicleId && it.yearMonth == installment.yearMonth) } + updated

        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.saveBankInstallment(updated)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun markInstallmentAsPaid(
        installment: FirestoreBankInstallment,
        paidAmount: Double,
        payDate: String,
        reference: String
    ) {
        val updated = installment.copy(
            amountPaid = paidAmount,
            paymentDate = payDate,
            isPaid = true,
            referenceOrTxnNo = reference
        )
        saveBankInstallment(updated)
    }

    fun deleteBankInstallment(installmentId: String) {
        _localInstallments.value = _localInstallments.value.filter { it.id != installmentId }
        if (_currentUser.value != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.deleteBankInstallment(installmentId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- Gemini AI Features ---

    fun setShowAiAdvisorDialog(show: Boolean) {
        _showAiAdvisorDialog.value = show
        if (show && _aiAdvisorResponse.value.isEmpty()) {
            _aiAdvisorResponse.value = if (_language.value == AppLanguage.NEPALI) {
                "नमस्ते हजुर! म तपाईंको 'सजिलो गाडी खाता' AI सल्लाहकार हुँ। गाडीको डिजेल बचत, आम्दानी बढाउने उपाय, चालक व्यवस्थापन वा बैंक किस्ताबारे जे पनि सोध्नुहोस्।"
            } else {
                "Hello! I am your Sajilo Gadi Khata AI Advisor. Ask me anything about saving fuel, maximizing profits, driver management, or bank EMI planning."
            }
        }
    }

    fun setAiAdvisorQuestion(q: String) {
        _aiAdvisorQuestion.value = q
    }

    fun askFleetAdvisor(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _isAiAdvisorLoading.value = true
            _aiAdvisorError.value = null
            _aiAdvisorResponse.value = ""

            val report = monthlyReport.value
            val vCount = allVehicles.value.size
            val summaryText = "Active Vehicles: $vCount. Month: ${report.yearMonth}. Income: Rs ${report.totalIncome}, Operating Exp: Rs ${report.totalOperatingExpenses} (Fuel: Rs ${report.totalFuel}), Driver Wages: Rs ${report.totalDriverWages}, EMI Paid: Rs ${report.totalBankEmiPaid}, Net: Rs ${report.netFleetProfit}."

            try {
                geminiService.askAdvisorStream(question, summaryText, _language.value).collect { chunkText ->
                    _aiAdvisorResponse.value = chunkText
                }
            } catch (e: TimeoutCancellationException) {
                _aiAdvisorError.value = "AI समय समाप्त भयो। कृपया पुन: सोध्नुहोस्।"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiAdvisorError.value = e.localizedMessage ?: "AI सेवामा समस्या आयो।"
            } finally {
                _isAiAdvisorLoading.value = false
            }
        }
    }

    fun setShowSmartVoiceEntryDialog(show: Boolean) {
        _showSmartVoiceEntryDialog.value = show
        if (show) {
            _parsedSmartEntry.value = null
            _smartParseError.value = null
        }
    }

    fun setSmartVoiceInputText(text: String) {
        _smartVoiceInputText.value = text
    }

    fun parseSmartVoiceEntry(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSmartParsing.value = true
            _smartParseError.value = null
            try {
                val knownRegs = allVehicles.value.map { it.regNumber }
                val result = geminiService.parseDailyKhataText(text, knownRegs)
                if (result.isSuccess) {
                    _parsedSmartEntry.value = result.getOrNull()
                } else {
                    _smartParseError.value = result.exceptionOrNull()?.localizedMessage ?: "हिसाब छुट्याउन सकिएन।"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _smartParseError.value = e.localizedMessage ?: "AI प्रविष्टि त्रुटि"
            } finally {
                _isSmartParsing.value = false
            }
        }
    }

    fun saveParsedSmartEntry(entry: ParsedSmartEntry, vehicleId: String) {
        val selectedVeh = allVehicles.value.find { it.id == vehicleId } ?: allVehicles.value.firstOrNull() ?: return
        val totalExp = entry.fuelExpense + entry.tollAndTaxExpense + entry.foodOrMiscExpense
        val log = FirestoreDailyLog(
            id = "log_${System.currentTimeMillis()}",
            vehicleId = selectedVeh.id,
            vehicleReg = selectedVeh.regNumber,
            date = selectedDate.value,
            grossIncome = entry.grossIncome,
            fuelExpense = entry.fuelExpense,
            evChargingExpense = 0.0,
            tollAndTaxExpense = entry.tollAndTaxExpense,
            foodOrMiscExpense = entry.foodOrMiscExpense,
            challanOrFineExpense = 0.0,
            maintenanceExpense = 0.0,
            helperWageExpense = 0.0,
            totalOperatingExpenses = totalExp,
            tripsOrRounds = entry.tripsOrRounds,
            driverWageEarned = 0.0,
            netDailyProfit = entry.grossIncome - totalExp,
            notes = if (entry.notes.isNotBlank()) entry.notes else "Smart AI Voice/Text Entry"
        )
        saveDailyLog(log)
        _showSmartVoiceEntryDialog.value = false
        _parsedSmartEntry.value = null
        _smartVoiceInputText.value = ""
    }

    fun generateMonthlyInsight(report: MonthlyReport) {
        viewModelScope.launch {
            _isGeneratingMonthlyInsight.value = true
            _monthlyInsightError.value = null
            try {
                val result = geminiService.generateMonthlyReportInsight(
                    monthYear = report.yearMonth,
                    totalIncome = report.totalIncome,
                    totalExpenses = report.totalOperatingExpenses,
                    netProfit = report.netFleetProfit,
                    vehicleCount = report.vehicleStats.size,
                    fuelTotal = report.totalFuel,
                    emiTotal = report.totalBankEmiPaid,
                    driverPayoutTotal = report.totalDriverWages,
                    language = _language.value
                )
                if (result.isSuccess) {
                    _monthlyAiInsight.value = result.getOrNull()
                } else {
                    _monthlyInsightError.value = result.exceptionOrNull()?.localizedMessage ?: "AI विश्लेषण असफल भयो।"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _monthlyInsightError.value = e.localizedMessage ?: "AI विश्लेषण त्रुटि"
            } finally {
                _isGeneratingMonthlyInsight.value = false
            }
        }
    }

    fun shiftMonth(step: Int) {
        val current = _selectedMonth.value
        try {
            val parts = current.split("-")
            var year = parts[0].toInt()
            var month = parts[1].toInt() + step
            if (month < 1) {
                month = 12
                year -= 1
            } else if (month > 12) {
                month = 1
                year += 1
            }
            _selectedMonth.value = String.format(Locale.US, "%04d-%02d", year, month)
        } catch (_: Exception) {}
    }

    fun shiftDate(stepDays: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = sdfDate.parse(_selectedDate.value)
            if (parsed != null) {
                cal.time = parsed
                cal.add(Calendar.DAY_OF_YEAR, stepDays)
                _selectedDate.value = sdfDate.format(cal.time)
            }
        } catch (_: Exception) {}
    }

    fun getConsolidatedBackupJson(): String {
        return firestoreRepo.createConsolidatedBackup(
            vehicles = allVehicles.value,
            logs = allDailyLogs.value,
            payouts = allDriverPayouts.value,
            installments = allInstallments.value,
            userEmail = _currentUser.value?.email
        )
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        val credentialManager = androidx.credentials.CredentialManager.create(context)
        AuthManager.signOut(context, credentialManager, viewModelScope) {
            _currentUser.value = null
            onComplete()
        }
    }

    private fun getSeedVehicles(): List<FirestoreVehicle> {
        return listOf(
            FirestoreVehicle(
                id = "seed_bus_1",
                regNumber = "BA-2-KHA 5521",
                nameOrModel = "City Super Bus (36-Seater)",
                type = "BUS",
                isElectric = false,
                routeOrArea = "Downtown - Ring Road Line",
                driverName = "Rajesh Sharma",
                driverPhone = "+977-9841234567",
                salaryType = "FIXED_DAILY",
                salaryRate = 1400.0,
                bankName = "Nabil Commercial Bank",
                monthlyEmi = 28000.0,
                emiDueDay = 10L,
                totalLoanAmount = 1800000.0
            ),
            FirestoreVehicle(
                id = "seed_ev_1",
                regNumber = "BA-1-JA 3490",
                nameOrModel = "Photon EV Microbus (14-Seater)",
                type = "EV_VAN",
                isElectric = true,
                routeOrArea = "Ratnapark - Bhaktapur Fast Route",
                driverName = "Bikash Gurung",
                driverPhone = "+977-9807654321",
                salaryType = "PERCENT_OF_INCOME",
                salaryRate = 16.0,
                bankName = "Everest Bank Ltd",
                monthlyEmi = 22500.0,
                emiDueDay = 15L,
                totalLoanAmount = 1450000.0
            ),
            FirestoreVehicle(
                id = "seed_taxi_1",
                regNumber = "BA-2-PA 8812",
                nameOrModel = "Hyundai Aura City Taxi",
                type = "TAXI",
                isElectric = false,
                routeOrArea = "Thamel - Airport Tourist Stand",
                driverName = "Sunil Thapa",
                driverPhone = "+977-9812398765",
                salaryType = "PERCENT_OF_NET",
                salaryRate = 25.0,
                bankName = "Standard Chartered",
                monthlyEmi = 16000.0,
                emiDueDay = 5L,
                totalLoanAmount = 950000.0
            )
        )
    }

    private fun getSeedDailyLogs(): List<FirestoreDailyLog> {
        val todayStr = sdfDate.format(Date())
        return listOf(
            FirestoreDailyLog(
                id = "log_bus_today",
                vehicleId = "seed_bus_1",
                vehicleReg = "BA-2-KHA 5521",
                date = todayStr,
                grossIncome = 9800.0,
                fuelExpense = 3400.0,
                tollAndTaxExpense = 350.0,
                maintenanceExpense = 1450.0,
                maintenanceWorkDetails = "Replaced front brake shoe, 4L Mobil 15W40 oil change, puncture repair",
                maintenanceBillNo = "BILL-8491",
                maintenanceVendor = "Balaju Auto Workshop",
                helperWageExpense = 600.0,
                foodOrMiscExpense = 200.0,
                totalOperatingExpenses = 3400.0 + 350.0 + 1450.0 + 600.0 + 200.0, // 6000.0
                driverWageEarned = 1400.0,
                driverPaidAmount = 1000.0,
                isDriverFullyPaid = false,
                netDailyProfit = 9800.0 - 6000.0 - 1400.0, // 2400.0
                tripsOrRounds = 6L,
                notes = "Heavy morning peak rush"
            ),
            FirestoreDailyLog(
                id = "log_ev_today",
                vehicleId = "seed_ev_1",
                vehicleReg = "BA-1-JA 3490",
                date = todayStr,
                grossIncome = 8200.0,
                fuelExpense = 0.0,
                isEvCharging = true,
                evUnitsKwh = 48.0,
                evRatePerUnit = 12.5,
                evChargingExpense = 48.0 * 12.5, // 600.0
                tollAndTaxExpense = 250.0,
                maintenanceExpense = 0.0,
                helperWageExpense = 500.0,
                foodOrMiscExpense = 150.0,
                totalOperatingExpenses = 600.0 + 250.0 + 500.0 + 150.0, // 1500.0
                driverWageEarned = 8200.0 * 0.16, // 1312.0
                driverPaidAmount = 1312.0,
                isDriverFullyPaid = true,
                netDailyProfit = 8200.0 - 1500.0 - 1312.0, // 5388.0
                tripsOrRounds = 8L,
                notes = "Fast DC charging at Sajha Charging Station"
            ),
            FirestoreDailyLog(
                id = "log_taxi_today",
                vehicleId = "seed_taxi_1",
                vehicleReg = "BA-2-PA 8812",
                date = todayStr,
                grossIncome = 5400.0,
                fuelExpense = 1800.0,
                tollAndTaxExpense = 120.0,
                foodOrMiscExpense = 100.0,
                totalOperatingExpenses = 2020.0,
                driverWageEarned = (5400.0 - 2020.0) * 0.25, // 845.0
                driverPaidAmount = 845.0,
                isDriverFullyPaid = true,
                netDailyProfit = 5400.0 - 2020.0 - 845.0, // 2535.0
                tripsOrRounds = 11L,
                notes = "Tourist stand & airport runs"
            )
        )
    }

    private fun getSeedDriverPayouts(): List<FirestoreDriverPayout> {
        val todayStr = sdfDate.format(Date())
        return listOf(
            FirestoreDriverPayout(
                id = "payout_seed_1",
                vehicleId = "seed_bus_1",
                vehicleReg = "BA-2-KHA 5521",
                driverName = "Rajesh Sharma",
                amount = 2000.0,
                date = todayStr,
                paymentMode = "Cash",
                notes = "Advance payment for family festival"
            )
        )
    }

    private fun getSeedInstallments(): List<FirestoreBankInstallment> {
        val currentMonthStr = sdfMonth.format(Date())
        return listOf(
            FirestoreBankInstallment(
                id = "emi_bus_current",
                vehicleId = "seed_bus_1",
                vehicleReg = "BA-2-KHA 5521",
                yearMonth = currentMonthStr,
                bankName = "Nabil Commercial Bank",
                amountDue = 28000.0,
                amountPaid = 28000.0,
                paymentDate = "$currentMonthStr-08",
                dueDay = 10L,
                isPaid = true,
                referenceOrTxnNo = "TXN-984210",
                notes = "Auto-debited from current account"
            ),
            FirestoreBankInstallment(
                id = "emi_ev_current",
                vehicleId = "seed_ev_1",
                vehicleReg = "BA-1-JA 3490",
                yearMonth = currentMonthStr,
                bankName = "Everest Bank Ltd",
                amountDue = 22500.0,
                amountPaid = 0.0,
                paymentDate = "",
                dueDay = 15L,
                isPaid = false,
                referenceOrTxnNo = "",
                notes = "Installment pending for this month"
            ),
            FirestoreBankInstallment(
                id = "emi_taxi_current",
                vehicleId = "seed_taxi_1",
                vehicleReg = "BA-2-PA 8812",
                yearMonth = currentMonthStr,
                bankName = "Standard Chartered",
                amountDue = 16000.0,
                amountPaid = 16000.0,
                paymentDate = "$currentMonthStr-05",
                dueDay = 5L,
                isPaid = true,
                referenceOrTxnNo = "SCB-77189",
                notes = "Paid via Mobile Banking"
            )
        )
    }
}
