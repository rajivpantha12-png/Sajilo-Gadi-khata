package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.example.ui.util.AppLanguage
import com.example.ui.util.NepaliStrings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.FirestoreDailyLog
import com.example.data.firestore.FirestoreVehicle
import com.example.ui.components.BreakdownRow
import com.example.ui.components.DateNavigator
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.VehicleFilterRow
import com.example.ui.components.formatMoney
import com.example.ui.components.getVehicleTypeIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.DailySummary

@Composable
fun DashboardScreen(
    summary: DailySummary,
    vehicles: List<FirestoreVehicle>,
    allLogs: List<FirestoreDailyLog>,
    selectedDate: String,
    selectedVehicleId: String?,
    currency: String,
    language: AppLanguage = AppLanguage.NEPALI,
    isFatherMode: Boolean = true,
    isSignedIn: Boolean = false,
    userEmail: String? = null,
    isUpdateAvailable: Boolean = false,
    onSelectVehicleFilter: (String?) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddLogClick: (String?) -> Unit,
    onEditLogClick: (FirestoreDailyLog) -> Unit,
    onDeleteLogClick: (FirestoreDailyLog) -> Unit,
    onSignInClick: () -> Unit = {},
    onShowDownloadShareClick: () -> Unit = {},
    onShowUpdateClick: () -> Unit = {},
    onToggleFatherMode: (Boolean) -> Unit = {},
    onQuickIncomeClick: () -> Unit = {},
    onQuickExpenseClick: () -> Unit = {},
    onShowAiAdvisorClick: () -> Unit = {},
    onShowSmartVoiceClick: () -> Unit = {},
    isDesktop: Boolean = false
) {
    val todayLogs = allLogs.filter {
        it.date == selectedDate && (selectedVehicleId == null || it.vehicleId == selectedVehicleId)
    }

    val displayVehicles = if (selectedVehicleId == null) {
        vehicles
    } else {
        vehicles.filter { it.id == selectedVehicleId }
    }

    if (isDesktop) {
        // Desktop / Widescreen Multi-Pane Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header: Date Navigator and Filter in a single spacious row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateNavigator(
                    currentDate = selectedDate,
                    onPrevious = onPreviousDay,
                    onNext = onNextDay,
                    modifier = Modifier.widthIn(max = 340.dp).testTag("desktop_date_navigator")
                )
                VehicleFilterRow(
                    vehicles = vehicles,
                    selectedVehicleId = selectedVehicleId,
                    onSelectVehicle = onSelectVehicleFilter,
                    modifier = Modifier.weight(1f).padding(start = 16.dp).testTag("desktop_vehicle_filter")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-Pane Content: Left pane for Vehicle Shift Cards, Right pane for Executive KPIs & Quick Actions
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Shift Cards & Logs
                LazyColumn(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vehicle Shifts & Trip Logs (${todayLogs.size}/${displayVehicles.size} Recorded)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (displayVehicles.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Vehicles Registered", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Add your bus, micro, or taxi in the Vehicles tab.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(displayVehicles, key = { it.id }) { vehicle ->
                            val vehicleLog = todayLogs.find { it.vehicleId == vehicle.id }
                            VehicleDailyCard(
                                vehicle = vehicle,
                                log = vehicleLog,
                                currency = currency,
                                onAddLog = { onAddLogClick(vehicle.id) },
                                onEditLog = { if (vehicleLog != null) onEditLogClick(vehicleLog) },
                                onDeleteLog = { if (vehicleLog != null) onDeleteLogClick(vehicleLog) }
                            )
                        }
                    }
                }

                // Right Column: Executive Daily Financial Summary & Operations Control
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Executive Daily Ledger KPI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 4 Stat Cards in 2x2 Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Daily Gross",
                            value = formatMoney(summary.totalIncome, currency),
                            subtitle = "${summary.logsCount} active shifts",
                            icon = Icons.Default.AttachMoney,
                            containerColor = ProfitGreenContainer,
                            contentColor = ProfitGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Operating Exp.",
                            value = formatMoney(summary.totalExpenses, currency),
                            subtitle = if (summary.totalEvChargingExpense > 0) "EV: ${formatMoney(summary.totalEvChargingExpense, currency)}" else "Fuel & repairs",
                            icon = if (summary.totalEvChargingExpense > 0) Icons.Default.ElectricCar else Icons.Default.LocalGasStation,
                            containerColor = ExpenseRedContainer,
                            contentColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Driver Salaries",
                            value = formatMoney(summary.totalDriverWage, currency),
                            subtitle = "Daily wages earned",
                            icon = Icons.Default.Person,
                            containerColor = WarningAmberContainer,
                            contentColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        val isPositiveNet = summary.netDailyProfit >= 0
                        StatCard(
                            title = "Daily Net Profit",
                            value = formatMoney(summary.netDailyProfit, currency),
                            subtitle = if (isPositiveNet) "Operating Profit" else "Operating Loss",
                            icon = Icons.Default.TrendingUp,
                            containerColor = if (isPositiveNet) MaterialTheme.colorScheme.primaryContainer else ExpenseRedContainer,
                            contentColor = if (isPositiveNet) MaterialTheme.colorScheme.primary else ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Desktop Quick Action Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Quick Daily Operations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Button(
                                onClick = { onAddLogClick(null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record New Daily Shift Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date Navigator Header
            item {
                DateNavigator(
                    currentDate = selectedDate,
                    onPrevious = onPreviousDay,
                    onNext = onNextDay,
                    modifier = Modifier.testTag("date_navigator")
                )
            }

            // Vehicle Filter Bar
            item {
                VehicleFilterRow(
                    vehicles = vehicles,
                    selectedVehicleId = selectedVehicleId,
                    onSelectVehicle = onSelectVehicleFilter,
                    modifier = Modifier.testTag("vehicle_filter_bar")
                )
            }

            // 1. Google Multi-Device Live Sync Status / Login Banner
            item {
                if (!isSignedIn) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_login_banner"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "गुगल खाता जोड्नुहोस् (Google Sign-In)" else "Connect Google Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "छोरा र बुबा दुवैको फोनमा हिसाब तुरुन्तै स्वतः अपडेट हुन्छ।" else "Real-time sync across Father and Son phones automatically.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onSignInClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "साइन-इन" else "Sign In",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ProfitGreenContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "☁️ $userEmail • दुवै फोनमा सिङ्क सक्रिय छ" else "☁️ $userEmail • Multi-device Live Sync Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfitGreen
                            )
                        }
                    }
                }
            }

            // 2. Auto-Update Notification Banner
            if (isUpdateAvailable) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShowUpdateClick() }
                            .testTag("dashboard_update_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "🚀 नयाँ अपडेट उपलब्ध छ (v1.1)" else "🚀 New App Update Available (v1.1)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "नयाँ सुविधा र सुधारहरू पाउन यहाँ थिच्नुहोस्" else "Tap to download the latest enhancements",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.NEPALI) "अपडेट ➔" else "Update ➔",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 3. Father's Senior Easy Actions Hub (Large, High-Contrast Buttons)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("father_easy_actions_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "बुबाको लागि सजिलो हिसाब" else "Senior Quick Operations",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isFatherMode) "सजिलो मोड" else "विस्तृत",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFatherMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = isFatherMode,
                                    onCheckedChange = onToggleFatherMode,
                                    modifier = Modifier.size(width = 44.dp, height = 24.dp)
                                )
                            }
                        }

                        // Big High-Contrast Buttons for Father
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onQuickIncomeClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .testTag("quick_income_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "कमाइ थप्नुहोस् (+)" else "Add Income (+)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = onQuickExpenseClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .testTag("quick_expense_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                            ) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "खर्च लेख्नुहोस् (-)" else "Add Expense (-)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // QR Code Download & Share Shortcut Button
                        OutlinedButton(
                            onClick = onShowDownloadShareClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("qr_share_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "📱 क्यामेराबाट QR स्क्यान गरी एप डाउनलोड गर्नुहोस्" else "📱 Scan QR Code to Download APK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Financial Summary Metrics 2x2 Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = NepaliStrings.todayIncome(language),
                            value = formatMoney(summary.totalIncome, currency),
                            subtitle = "${summary.logsCount} ${if (language == AppLanguage.NEPALI) "शिफ्ट रेकर्ड" else "shifts"}",
                            icon = Icons.Default.AttachMoney,
                            containerColor = ProfitGreenContainer,
                            contentColor = ProfitGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = NepaliStrings.todayExpense(language),
                            value = formatMoney(summary.totalExpenses, currency),
                            subtitle = if (summary.totalEvChargingExpense > 0) "EV: ${formatMoney(summary.totalEvChargingExpense, currency)}" else if (language == AppLanguage.NEPALI) "इन्धन र मर्मत" else "Operating costs",
                            icon = if (summary.totalEvChargingExpense > 0) Icons.Default.ElectricCar else Icons.Default.LocalGasStation,
                            containerColor = ExpenseRedContainer,
                            contentColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = if (language == AppLanguage.NEPALI) "चालकको दैनिक तलब" else "Driver Salaries",
                            value = formatMoney(summary.totalDriverWage, currency),
                            subtitle = if (language == AppLanguage.NEPALI) "कमाइको आधारमा" else "Daily wages earned",
                            icon = Icons.Default.Person,
                            containerColor = WarningAmberContainer,
                            contentColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        val isPositiveNet = summary.netDailyProfit >= 0
                        StatCard(
                            title = NepaliStrings.netSavings(language),
                            value = formatMoney(summary.netDailyProfit, currency),
                            subtitle = if (isPositiveNet) (if (language == AppLanguage.NEPALI) "नाफा (बचत)" else "Operating Profit") else (if (language == AppLanguage.NEPALI) "घाटा" else "Operating Loss"),
                            icon = Icons.Default.TrendingUp,
                            containerColor = if (isPositiveNet) MaterialTheme.colorScheme.primaryContainer else ExpenseRedContainer,
                            contentColor = if (isPositiveNet) MaterialTheme.colorScheme.primary else ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section Header: Fleet Daily Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vehicle Shifts (${todayLogs.size}/${displayVehicles.size} Recorded)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Vehicle Log Cards
            if (displayVehicles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Vehicles Registered",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Go to Vehicles tab to add your bus, micro, or taxi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayVehicles, key = { it.id }) { vehicle ->
                    val vehicleLog = todayLogs.find { it.vehicleId == vehicle.id }
                    VehicleDailyCard(
                        vehicle = vehicle,
                        log = vehicleLog,
                        currency = currency,
                        onAddLog = { onAddLogClick(vehicle.id) },
                        onEditLog = { if (vehicleLog != null) onEditLogClick(vehicleLog) },
                        onDeleteLog = { if (vehicleLog != null) onDeleteLogClick(vehicleLog) }
                    )
                }
            }
        }

        // Floating Action Button to record entry
        FloatingActionButton(
            onClick = { onAddLogClick(null) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_quick_daily_entry")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Log")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Daily Log", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VehicleDailyCard(
    vehicle: FirestoreVehicle,
    log: FirestoreDailyLog?,
    currency: String,
    onAddLog: () -> Unit,
    onEditLog: () -> Unit,
    onDeleteLog: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vehicle_daily_card_${vehicle.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Vehicle Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getVehicleTypeIcon(vehicle.type, vehicle.isElectric),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.regNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (vehicle.isElectric) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "⚡ EV",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${vehicle.nameOrModel} • Driver: ${vehicle.driverName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (log != null) {
                    StatusBadge(text = "Logged", isSuccess = true)
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarningAmberContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (log != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Gross Income", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatMoney(log.grossIncome, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }
                        Column {
                            Text("Expenses", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatMoney(log.totalOperatingExpenses, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                        Column {
                            Text("Driver Wage", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatMoney(log.driverWageEarned, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                        Column {
                            Text("Net Profit", style = MaterialTheme.typography.labelSmall)
                            val isNetPos = log.netDailyProfit >= 0
                            Text(
                                text = formatMoney(log.netDailyProfit, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isNetPos) MaterialTheme.colorScheme.primary else ExpenseRed
                            )
                        }
                    }
                }

                // Expandable detailed breakdown
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Itemized Expense & Repair Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (log.isEvCharging && log.evChargingExpense > 0) {
                            BreakdownRow("⚡ EV Charging (${log.evUnitsKwh} kWh @ $currency${log.evRatePerUnit})", log.evChargingExpense, currency)
                        }
                        if (log.fuelExpense > 0) {
                            BreakdownRow("Fuel (Diesel/Petrol)", log.fuelExpense, currency)
                        }
                        BreakdownRow("Tolls & Parking", log.tollAndTaxExpense, currency)

                        if (log.maintenanceExpense > 0) {
                            BreakdownRow("Maintenance & Repairs", log.maintenanceExpense, currency)
                            if (log.maintenanceWorkDetails.isNotBlank()) {
                                Text(
                                    text = "   🔧 Work done: ${log.maintenanceWorkDetails}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (log.maintenanceBillNo.isNotBlank() || log.maintenanceVendor.isNotBlank()) {
                                Text(
                                    text = "   🧾 Bill: ${log.maintenanceBillNo} • Garage: ${log.maintenanceVendor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (log.challanOrFineExpense > 0) {
                            BreakdownRow("Challan / Police Fine", log.challanOrFineExpense, currency)
                        }
                        if (log.helperWageExpense > 0) {
                            BreakdownRow("Conductor / Helper Wage", log.helperWageExpense, currency)
                        }
                        if (log.foodOrMiscExpense > 0) {
                            BreakdownRow("Food & Misc", log.foodOrMiscExpense, currency)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Driver Wage and Paid status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Driver Wage Earned:", style = MaterialTheme.typography.bodySmall)
                            Text(formatMoney(log.driverWageEarned, currency), fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Driver Cash Handed Over:", style = MaterialTheme.typography.bodySmall)
                            Text(formatMoney(log.driverPaidAmount, currency), color = ProfitGreen)
                        }
                        val unpaidWage = (log.driverWageEarned - log.driverPaidAmount).coerceAtLeast(0.0)
                        if (unpaidWage > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pending Wage Balance:", style = MaterialTheme.typography.bodySmall, color = ExpenseRed)
                                Text(formatMoney(unpaidWage, currency), color = ExpenseRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notes: ${log.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide details" else "View details (${log.tripsOrRounds} trips)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { expanded = !expanded }
                            .padding(4.dp)
                    )

                    Row {
                        IconButton(onClick = onEditLog, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Log", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDeleteLog, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Log", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No shift logged for today yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onAddLog,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log Today's Shift", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
