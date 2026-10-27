package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.FirestoreBankInstallment
import com.example.data.firestore.FirestoreVehicle
import com.example.ui.components.MonthNavigator
import com.example.ui.components.StatusBadge
import com.example.ui.components.VehicleFilterRow
import com.example.ui.components.formatMoney
import com.example.ui.components.getVehicleTypeIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.MonthlyReport
import com.example.ui.viewmodel.VehicleMonthlyStat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyReportsScreen(
    report: MonthlyReport,
    vehicles: List<FirestoreVehicle>,
    installments: List<FirestoreBankInstallment>,
    selectedMonth: String,
    selectedVehicleId: String?,
    currency: String,
    onSelectVehicleFilter: (String?) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMarkEmiPaid: (FirestoreBankInstallment, Double, String, String) -> Unit,
    onGetBackupJson: () -> String,
    isDesktop: Boolean = false
) {
    val context = LocalContext.current
    var payingInstallment by remember { mutableStateOf<FirestoreBankInstallment?>(null) }

    val monthInstallments = vehicles.filter {
        selectedVehicleId == null || it.id == selectedVehicleId
    }.map { v ->
        val existing = installments.find { it.vehicleId == v.id && it.yearMonth == selectedMonth }
        existing ?: FirestoreBankInstallment(
            vehicleId = v.id,
            vehicleReg = v.regNumber,
            yearMonth = selectedMonth,
            bankName = v.bankName.ifBlank { "Commercial Bank" },
            amountDue = v.monthlyEmi,
            amountPaid = 0.0,
            dueDay = v.emiDueDay,
            isPaid = false
        )
    }

    if (isDesktop) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthNavigator(
                    currentMonth = selectedMonth,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                    modifier = Modifier.widthIn(max = 340.dp).testTag("desktop_month_navigator")
                )
                VehicleFilterRow(
                    vehicles = vehicles,
                    selectedVehicleId = selectedVehicleId,
                    onSelectVehicle = onSelectVehicleFilter,
                    modifier = Modifier.weight(1f).padding(start = 16.dp).testTag("desktop_report_filter")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-Column Desktop Report
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Executive P&L + Vehicle Breakdown
                LazyColumn(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        ExecutiveReportCard(
                            report = report,
                            currency = currency,
                            onShare = { shareReport(context, report, currency) }
                        )
                    }

                    item {
                        // Backup Download Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Consolidated Single-File Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Exports all fleet shifts, EV charging & repair bills", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Button(
                                    onClick = {
                                        val backupJson = onGetBackupJson()
                                        shareBackupFile(context, backupJson)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Download Backup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Vehicle Profit Contribution (${report.vehicleStats.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    if (report.vehicleStats.isEmpty()) {
                        item {
                            Text("No operational logs recorded for $selectedMonth.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(report.vehicleStats, key = { it.vehicle.id }) { stat ->
                            VehicleMonthlyPerformanceCard(stat = stat, currency = currency)
                        }
                    }
                }

                // Right Column: Bank Loan Installment Status (EMI)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text("Bank Loan Installments (EMI) - $selectedMonth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(monthInstallments, key = { it.vehicleId }) { installment ->
                        val v = vehicles.find { it.id == installment.vehicleId }
                        BankEmiCard(
                            installment = installment,
                            vehicle = v,
                            currency = currency,
                            onPayClick = { payingInstallment = installment }
                        )
                    }
                }
            }
        }

        if (payingInstallment != null) {
            PayEmiDialog(
                installment = payingInstallment!!,
                currency = currency,
                onDismiss = { payingInstallment = null },
                onConfirm = { amount, date, ref ->
                    onMarkEmiPaid(payingInstallment!!, amount, date, ref)
                    payingInstallment = null
                }
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Navigator
        item {
            MonthNavigator(
                currentMonth = selectedMonth,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                modifier = Modifier.testTag("month_navigator")
            )
        }

        // Vehicle Filter Bar
        item {
            VehicleFilterRow(
                vehicles = vehicles,
                selectedVehicleId = selectedVehicleId,
                onSelectVehicle = onSelectVehicleFilter,
                modifier = Modifier.testTag("report_vehicle_filter")
            )
        }

        // Consolidated Backup Download Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Consolidated Single-File Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Exports all fleet vehicles, shifts, EV & bills into 1 file", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Button(
                        onClick = {
                            val backupJson = onGetBackupJson()
                            shareBackupFile(context, backupJson)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Download Backup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Executive Financial Overview Card
        item {
            ExecutiveReportCard(
                report = report,
                currency = currency,
                onShare = {
                    shareReport(context, report, currency)
                }
            )
        }

        // Bank Loan EMI Status Section
        item {
            Text(
                text = "Bank Loan Installments (EMI) - $selectedMonth",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(monthInstallments, key = { it.vehicleId }) { installment ->
            val v = vehicles.find { it.id == installment.vehicleId }
            BankEmiCard(
                installment = installment,
                vehicle = v,
                currency = currency,
                onPayClick = { payingInstallment = installment }
            )
        }

        // Vehicle Performance & Net Profit Breakdown
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Earnings Per Vehicle Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (report.vehicleStats.isEmpty()) {
            item {
                Text(
                    text = "No operational logs recorded for $selectedMonth.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(report.vehicleStats, key = { it.vehicle.id }) { stat ->
                VehicleMonthlyPerformanceCard(stat = stat, currency = currency)
            }
        }
    }

    // Pay EMI Dialog
    if (payingInstallment != null) {
        PayEmiDialog(
            installment = payingInstallment!!,
            currency = currency,
            onDismiss = { payingInstallment = null },
            onConfirm = { amount, date, ref ->
                onMarkEmiPaid(payingInstallment!!, amount, date, ref)
                payingInstallment = null
            }
        )
    }
}

@Composable
fun ExecutiveReportCard(
    report: MonthlyReport,
    currency: String,
    onShare: () -> Unit
) {
    val isNetProfit = report.netFleetProfit >= 0
    val netColor = if (isNetProfit) ProfitGreen else ExpenseRed
    val netContainer = if (isNetProfit) ProfitGreenContainer else ExpenseRedContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("executive_report_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Share button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Monthly Profit & Loss",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Month: ${report.yearMonth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_report_button")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share Report",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Net Profit Hero Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = netContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isNetProfit) "TRUE NET PROFIT" else "TOTAL NET LOSS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = netColor
                        )
                        Text(
                            text = "After fuel/EV, repairs, driver & bank EMI",
                            style = MaterialTheme.typography.bodySmall,
                            color = netColor.copy(alpha = 0.85f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatMoney(report.netFleetProfit, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = netColor
                        )
                        if (report.totalIncome > 0) {
                            Text(
                                text = "Margin: ${String.format(Locale.US, "%.1f", report.profitMarginPercent)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = netColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Income and Expense Line Items
            ReportLineItem("Gross Fares Collected", report.totalIncome, currency, ProfitGreen, true)
            Spacer(modifier = Modifier.height(4.dp))
            if (report.totalFuel > 0) {
                ReportLineItem("Fuel (Diesel/Petrol)", report.totalFuel, currency, ExpenseRed, false)
            }
            if (report.totalEvCharging > 0) {
                ReportLineItem("⚡ EV Charging", report.totalEvCharging, currency, InfoBlue, false)
            }
            ReportLineItem("Tolls, Parking & Taxes", report.totalToll, currency, ExpenseRed, false)
            ReportLineItem("Maintenance & Repairs", report.totalMaintenance, currency, ExpenseRed, false)
            if (report.totalOtherExpenses > 0) {
                ReportLineItem("Challan, Helper & Misc", report.totalOtherExpenses, currency, ExpenseRed, false)
            }
            ReportLineItem("Driver Wages (Calculated)", report.totalDriverWages, currency, WarningAmber, false)
            ReportLineItem("Bank Loan EMIs Paid", report.totalBankEmiPaid, currency, InfoBlue, false)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Operating Expenses:", style = MaterialTheme.typography.bodySmall)
                Text(formatMoney(report.totalOperatingExpenses, currency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Loan Installments Paid:", style = MaterialTheme.typography.bodySmall)
                Text(formatMoney(report.totalBankEmiPaid, currency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ReportLineItem(
    title: String,
    amount: Double,
    currency: String,
    color: Color,
    isPlus: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${if (isPlus) "+" else "-"} ${formatMoney(amount, currency)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun BankEmiCard(
    installment: FirestoreBankInstallment,
    vehicle: FirestoreVehicle?,
    currency: String,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emi_card_${installment.vehicleId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (installment.isPaid) ProfitGreenContainer else WarningAmberContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (installment.isPaid) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (installment.isPaid) ProfitGreen else WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = vehicle?.regNumber ?: installment.vehicleReg.ifBlank { "Vehicle" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${installment.bankName} • Due: ${installment.dueDay}th of month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (installment.isPaid && installment.paymentDate.isNotBlank()) {
                        Text(
                            text = "Paid on ${installment.paymentDate} ${if (installment.referenceOrTxnNo.isNotBlank()) "(${installment.referenceOrTxnNo})" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfitGreen
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(installment.amountDue, currency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (installment.isPaid) ProfitGreen else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (installment.isPaid) {
                    StatusBadge(text = "Paid", isSuccess = true)
                } else {
                    OutlinedButton(
                        onClick = onPayClick,
                        modifier = Modifier.testTag("pay_emi_btn_${installment.vehicleId}")
                    ) {
                        Text("Pay EMI", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleMonthlyPerformanceCard(
    stat: VehicleMonthlyStat,
    currency: String
) {
    val isProfit = stat.netVehicleProfit >= 0
    val netColor = if (isProfit) ProfitGreen else ExpenseRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vehicle_performance_${stat.vehicle.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            getVehicleTypeIcon(stat.vehicle.type, stat.vehicle.isElectric),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stat.vehicle.regNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (stat.vehicle.isElectric) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("⚡", fontSize = 12.sp)
                            }
                        }
                        Text(
                            "${stat.vehicle.nameOrModel} • ${stat.daysOperated} days active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Vehicle Net", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = formatMoney(stat.netVehicleProfit, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Fares:", style = MaterialTheme.typography.bodySmall)
                        Text(formatMoney(stat.grossIncome, currency), fontWeight = FontWeight.SemiBold, color = ProfitGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Operating Costs (Fuel/EV/Repairs):", style = MaterialTheme.typography.bodySmall)
                        Text("- ${formatMoney(stat.totalOperatingExpenses, currency)}", color = ExpenseRed)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Driver Wage (${stat.vehicle.driverName}):", style = MaterialTheme.typography.bodySmall)
                        Text("- ${formatMoney(stat.driverWageEarned, currency)}", color = WarningAmber)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bank Loan EMI Paid:", style = MaterialTheme.typography.bodySmall)
                        Text("- ${formatMoney(stat.bankEmiPaid, currency)}", color = InfoBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun PayEmiDialog(
    installment: FirestoreBankInstallment,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var amountText by remember { mutableStateOf(installment.amountDue.toString()) }
    var payDateText by remember { mutableStateOf(sdfDate.format(Date())) }
    var refText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Bank Loan EMI Payment", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Bank: ${installment.bankName} (Due day: ${installment.dueDay}th)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid") },
                    prefix = { Text("$currency ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payDateText,
                    onValueChange = { payDateText = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = refText,
                    onValueChange = { refText = it },
                    label = { Text("Bank Reference / Txn ID (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: installment.amountDue
                    onConfirm(amount, payDateText.trim(), refText.trim())
                }
            ) {
                Text("Confirm EMI Paid")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun shareReport(context: Context, report: MonthlyReport, currency: String) {
    val sb = StringBuilder()
    sb.append("📊 FLEET FINANCIAL REPORT - ${report.yearMonth}\n")
    sb.append("=========================================\n")
    sb.append("Gross Fares Collected: ${formatMoney(report.totalIncome, currency)}\n")
    if (report.totalFuel > 0) sb.append("Fuel Expenses:         - ${formatMoney(report.totalFuel, currency)}\n")
    if (report.totalEvCharging > 0) sb.append("⚡ EV Charging:         - ${formatMoney(report.totalEvCharging, currency)}\n")
    sb.append("Tolls & Parking:       - ${formatMoney(report.totalToll, currency)}\n")
    sb.append("Repairs & Maintenance: - ${formatMoney(report.totalMaintenance, currency)}\n")
    sb.append("Other Operating Exp:   - ${formatMoney(report.totalOtherExpenses, currency)}\n")
    sb.append("Driver Wages:          - ${formatMoney(report.totalDriverWages, currency)}\n")
    sb.append("Bank Loan EMIs Paid:   - ${formatMoney(report.totalBankEmiPaid, currency)}\n")
    sb.append("-----------------------------------------\n")
    sb.append("TRUE NET PROFIT:       ${formatMoney(report.netFleetProfit, currency)}\n")
    sb.append("Profit Margin:         ${String.format(Locale.US, "%.1f", report.profitMarginPercent)}%\n")
    sb.append("=========================================\n\n")

    sb.append("VEHICLE-BY-VEHICLE NET EARNINGS:\n")
    report.vehicleStats.forEach { stat ->
        sb.append("• ${stat.vehicle.regNumber} (${stat.vehicle.nameOrModel}):\n")
        sb.append("   Income: ${formatMoney(stat.grossIncome, currency)} | Operating: -${formatMoney(stat.totalOperatingExpenses, currency)}\n")
        sb.append("   Driver: -${formatMoney(stat.driverWageEarned, currency)} | EMI: -${formatMoney(stat.bankEmiPaid, currency)}\n")
        sb.append("   Net Profit: ${formatMoney(stat.netVehicleProfit, currency)}\n\n")
    }

    sb.append("Generated by FleetLedger Cloud App")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Fleet Report - ${report.yearMonth}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Monthly Report"))
}

fun shareBackupFile(context: Context, backupJson: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_SUBJECT, "FleetLedger_Consolidated_Backup.json")
        putExtra(Intent.EXTRA_TEXT, backupJson)
    }
    context.startActivity(Intent.createChooser(intent, "Download / Share Consolidated Backup"))
}
