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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.FirestoreDriverPayout
import com.example.data.firestore.FirestoreVehicle
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatMoney
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.DriverDailyRecord
import com.example.ui.viewmodel.DriverSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverScreen(
    driverSummaries: List<DriverSummary>,
    vehicles: List<FirestoreVehicle>,
    allPayouts: List<FirestoreDriverPayout>,
    currency: String,
    onRecordPayout: (String, String, Double, String, String, String) -> Unit,
    onDeletePayout: (String) -> Unit,
    isDesktop: Boolean = false
) {
    val totalEarned = driverSummaries.sumOf { it.totalWageEarned }
    val totalPaid = driverSummaries.sumOf { it.totalPaidOverall }
    val totalPending = (totalEarned - totalPaid).coerceAtLeast(0.0)

    var showPayoutDialog by remember { mutableStateOf(false) }
    var preselectedDriver by remember { mutableStateOf<DriverSummary?>(null) }

    if (isDesktop) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Overview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Wages Earned",
                    value = formatMoney(totalEarned, currency),
                    subtitle = "All driver daily shifts",
                    icon = Icons.Default.Person,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Paid to Drivers",
                    value = formatMoney(totalPaid, currency),
                    subtitle = "Cash & advances paid",
                    icon = Icons.Default.CheckCircle,
                    containerColor = ProfitGreenContainer,
                    contentColor = ProfitGreen,
                    modifier = Modifier.weight(1f)
                )
                val hasPending = totalPending > 0
                val bannerBg = if (hasPending) WarningAmberContainer else ProfitGreenContainer
                val bannerColor = if (hasPending) WarningAmber else ProfitGreen
                StatCard(
                    title = if (hasPending) "Pending Due Wages" else "All Wages Settled",
                    value = formatMoney(totalPending, currency),
                    subtitle = if (hasPending) "Outstanding balance" else "Zero balance",
                    icon = if (hasPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                    containerColor = bannerBg,
                    contentColor = bannerColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Two-Column Desktop Layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Driver Statements
                LazyColumn(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Driver Day-Wise Statements (${driverSummaries.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(driverSummaries) { driver ->
                        DriverCard(
                            driver = driver,
                            currency = currency,
                            onPayDriver = {
                                preselectedDriver = driver
                                showPayoutDialog = true
                            }
                        )
                    }
                }

                // Right Column: Payout History & Fast Action
                LazyColumn(
                    modifier = Modifier
                        .weight(0.9f)
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
                                text = "Advance & Settlement History (${allPayouts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    preselectedDriver = null
                                    showPayoutDialog = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pay Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (allPayouts.isEmpty()) {
                        item {
                            Text(
                                text = "No additional driver payouts recorded yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(allPayouts, key = { it.id }) { payout ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${payout.driverName} • ${payout.date}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mode: ${payout.paymentMode} ${if (payout.notes.isNotBlank()) "• ${payout.notes}" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatMoney(payout.amount, currency),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ProfitGreen
                                        )
                                        IconButton(onClick = { onDeletePayout(payout.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPayoutDialog) {
            RecordPayoutDialog(
                drivers = driverSummaries,
                initialDriver = preselectedDriver,
                currency = currency,
                onDismiss = { showPayoutDialog = false },
                onSave = { vId, dName, amount, date, mode, notes ->
                    onRecordPayout(vId, dName, amount, date, mode, notes)
                    showPayoutDialog = false
                }
            )
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
            // Overview statistics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Wages Earned",
                        value = formatMoney(totalEarned, currency),
                        subtitle = "Calculated daily wages",
                        icon = Icons.Default.Person,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Paid to Drivers",
                        value = formatMoney(totalPaid, currency),
                        subtitle = "Cash & advances with dates",
                        icon = Icons.Default.CheckCircle,
                        containerColor = ProfitGreenContainer,
                        contentColor = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending balance banner
            item {
                val hasPending = totalPending > 0
                val bannerBg = if (hasPending) WarningAmberContainer else ProfitGreenContainer
                val bannerColor = if (hasPending) WarningAmber else ProfitGreen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = bannerColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (hasPending) "Pending Driver Wages (Dina Baki)" else "All Driver Wages Settled",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerColor
                                )
                                Text(
                                    text = if (hasPending) "Calculated day-by-day across all shifts" else "Zero outstanding wage balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = bannerColor.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Text(
                            text = formatMoney(totalPending, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )
                    }
                }
            }

            // Section: Drivers list
            item {
                Text(
                    text = "Driver Day-Wise Statements (${driverSummaries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(driverSummaries) { driver ->
                DriverCard(
                    driver = driver,
                    currency = currency,
                    onPayDriver = {
                        preselectedDriver = driver
                        showPayoutDialog = true
                    }
                )
            }

            // Section: Payout History
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Driver Advance & Settlement History (${allPayouts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (allPayouts.isEmpty()) {
                item {
                    Text(
                        text = "No additional driver payouts recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(allPayouts, key = { it.id }) { payout ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${payout.driverName} • Date: ${payout.date}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mode: ${payout.paymentMode} ${if (payout.notes.isNotBlank()) "• ${payout.notes}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatMoney(payout.amount, currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                                IconButton(onClick = { onDeletePayout(payout.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to record payout
        FloatingActionButton(
            onClick = {
                preselectedDriver = null
                showPayoutDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_record_driver_payout")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pay Driver", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Payout Dialog
    if (showPayoutDialog) {
        RecordPayoutDialog(
            drivers = driverSummaries,
            initialDriver = preselectedDriver,
            currency = currency,
            onDismiss = { showPayoutDialog = false },
            onSave = { vId, dName, amount, date, mode, notes ->
                onRecordPayout(vId, dName, amount, date, mode, notes)
                showPayoutDialog = false
            }
        )
    }
}

@Composable
fun DriverCard(
    driver: DriverSummary,
    currency: String,
    onPayDriver: () -> Unit
) {
    var expandedStatement by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_card_${driver.vehicleId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = driver.driverName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assigned: ${driver.vehicleReg} ${if (driver.phone.isNotBlank()) "• ${driver.phone}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isSettled = driver.pendingBalance <= 0
                StatusBadge(
                    text = if (isSettled) "Settled" else "Pending Due",
                    isSuccess = isSettled
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wage Breakdown Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Earned", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = formatMoney(driver.totalWageEarned, currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("Total Paid (Liyeako)", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = formatMoney(driver.totalPaidOverall, currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                    }
                    Column {
                        Text("Pending (Dina Baki)", style = MaterialTheme.typography.labelSmall)
                        val dueColor = if (driver.pendingBalance > 0) ExpenseRed else ProfitGreen
                        Text(
                            text = formatMoney(driver.pendingBalance.coerceAtLeast(0.0), currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = dueColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle Date-Wise Statement
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedStatement = !expandedStatement }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (expandedStatement) "Hide Day-Wise Breakdown" else "View Date-Wise Ledger (${driver.dailyRecords.size} shifts)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expandedStatement) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded Date-by-Date Ledger Table
            AnimatedVisibility(visible = expandedStatement) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📅 Date-by-Date Shift Wage & Payment Breakdown:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (driver.dailyRecords.isEmpty()) {
                        Text(
                            "No daily shifts recorded for this driver yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        driver.dailyRecords.forEach { rec ->
                            DailyWageItemRow(record = rec, currency = currency)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pay / Settle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onPayDriver,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("pay_driver_btn_${driver.vehicleId}")
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay Driver / Settle", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DailyWageItemRow(record: DriverDailyRecord, currency: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Date: ${record.date}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Earned: ${formatMoney(record.wageEarned, currency)} | Paid: ${formatMoney(record.amountPaid, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (record.unpaidDailyBalance > 0) {
                Text(
                    text = "Due: ${formatMoney(record.unpaidDailyBalance, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            } else {
                Text(
                    text = "✓ Paid",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )
            }
        }
    }
}

@Composable
fun RecordPayoutDialog(
    drivers: List<DriverSummary>,
    initialDriver: DriverSummary?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, String, String) -> Unit
) {
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDriver by remember { mutableStateOf(initialDriver ?: drivers.firstOrNull()) }
    var amountText by remember {
        val initialAmount = if (initialDriver != null && initialDriver.pendingBalance > 0) {
            initialDriver.pendingBalance.toString()
        } else ""
        mutableStateOf(initialAmount)
    }
    var dateText by remember { mutableStateOf(sdfDate.format(Date())) }
    var paymentMode by remember { mutableStateOf("Cash") }
    var notesText by remember { mutableStateOf("") }

    if (selectedDriver == null) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Payment to Driver (Paisa Deako)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Driver: ${selectedDriver?.driverName} (${selectedDriver?.vehicleReg})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if ((selectedDriver?.pendingBalance ?: 0.0) > 0) {
                    Text(
                        text = "Current pending wage: ${formatMoney(selectedDriver!!.pendingBalance, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount ($currency)") },
                    prefix = { Text("$currency ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payout_amount")
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payout_date")
                )

                OutlinedTextField(
                    value = paymentMode,
                    onValueChange = { paymentMode = it },
                    label = { Text("Payment Mode (Cash / eSewa / Khalti / Bank)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Receipt (e.g. Weekly settlement)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && selectedDriver != null) {
                        onSave(
                            selectedDriver!!.vehicleId,
                            selectedDriver!!.driverName,
                            amount,
                            dateText.trim(),
                            paymentMode.trim(),
                            notesText.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_payout_button")
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
