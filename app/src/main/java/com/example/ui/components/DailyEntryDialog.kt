package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.firestore.FirestoreDailyLog
import com.example.data.firestore.FirestoreVehicle
import com.example.data.model.SalaryType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer

@Composable
fun DailyEntryDialog(
    vehicles: List<FirestoreVehicle>,
    initialLog: FirestoreDailyLog? = null,
    defaultVehicleId: String? = null,
    defaultDate: String,
    currency: String,
    onCalculateWage: (String, Double, Double, Double) -> Double,
    onDismiss: () -> Unit,
    onSave: (FirestoreDailyLog) -> Unit
) {
    if (vehicles.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Vehicles Found") },
            text = { Text("Please add at least one vehicle before recording a daily log.") },
            confirmButton = {
                Button(onClick = onDismiss) { Text("OK") }
            }
        )
        return
    }

    var selectedVehicle by remember {
        val initialV = if (initialLog != null) {
            vehicles.find { it.id == initialLog.vehicleId }
        } else if (defaultVehicleId != null) {
            vehicles.find { it.id == defaultVehicleId }
        } else null
        mutableStateOf(initialV ?: vehicles.first())
    }

    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf(initialLog?.date ?: defaultDate) }
    var grossIncomeText by remember { mutableStateOf(if (initialLog != null && initialLog.grossIncome > 0) initialLog.grossIncome.toString() else "") }

    // Fuel & EV Charging
    var isEvCharging by remember { mutableStateOf(initialLog?.isEvCharging ?: selectedVehicle.isElectric) }
    var evUnitsText by remember { mutableStateOf(if (initialLog != null && initialLog.evUnitsKwh > 0) initialLog.evUnitsKwh.toString() else "") }
    var evRateText by remember { mutableStateOf(if (initialLog != null && initialLog.evRatePerUnit > 0) initialLog.evRatePerUnit.toString() else "12") }
    var fuelText by remember { mutableStateOf(if (initialLog != null && initialLog.fuelExpense > 0) initialLog.fuelExpense.toString() else "") }

    // Other Expenses
    var tollText by remember { mutableStateOf(if (initialLog != null && initialLog.tollAndTaxExpense > 0) initialLog.tollAndTaxExpense.toString() else "") }
    var fineText by remember { mutableStateOf(if (initialLog != null && initialLog.challanOrFineExpense > 0) initialLog.challanOrFineExpense.toString() else "") }
    var helperWageText by remember { mutableStateOf(if (initialLog != null && initialLog.helperWageExpense > 0) initialLog.helperWageExpense.toString() else "") }
    var miscText by remember { mutableStateOf(if (initialLog != null && initialLog.foodOrMiscExpense > 0) initialLog.foodOrMiscExpense.toString() else "") }

    // Detailed Maintenance / Repairs with Bills & Work Done
    var maintenanceText by remember { mutableStateOf(if (initialLog != null && initialLog.maintenanceExpense > 0) initialLog.maintenanceExpense.toString() else "") }
    var maintenanceWorkText by remember { mutableStateOf(initialLog?.maintenanceWorkDetails ?: "") }
    var maintenanceBillNoText by remember { mutableStateOf(initialLog?.maintenanceBillNo ?: "") }
    var maintenanceVendorText by remember { mutableStateOf(initialLog?.maintenanceVendor ?: "") }

    var tripsCountText by remember { mutableStateOf(initialLog?.tripsOrRounds?.toString() ?: "1") }
    var notesText by remember { mutableStateOf(initialLog?.notes ?: "") }

    // Driver wage & payment
    var customWageOverride by remember { mutableStateOf(false) }
    var customWageText by remember { mutableStateOf(initialLog?.driverWageEarned?.toString() ?: "") }
    var driverPaidText by remember { mutableStateOf(initialLog?.driverPaidAmount?.toString() ?: "") }
    var hasManuallyEditedPaid by remember { mutableStateOf(initialLog != null) }

    // Parsed calculations
    val grossIncome by remember { derivedStateOf { grossIncomeText.toDoubleOrNull() ?: 0.0 } }
    val fuelExp by remember { derivedStateOf { if (!isEvCharging) (fuelText.toDoubleOrNull() ?: 0.0) else 0.0 } }
    val evUnits by remember { derivedStateOf { evUnitsText.toDoubleOrNull() ?: 0.0 } }
    val evRate by remember { derivedStateOf { evRateText.toDoubleOrNull() ?: 0.0 } }
    val evChargingExp by remember { derivedStateOf { if (isEvCharging) (evUnits * evRate) else 0.0 } }

    val tollExp by remember { derivedStateOf { tollText.toDoubleOrNull() ?: 0.0 } }
    val maintExp by remember { derivedStateOf { maintenanceText.toDoubleOrNull() ?: 0.0 } }
    val fineExp by remember { derivedStateOf { fineText.toDoubleOrNull() ?: 0.0 } }
    val helperExp by remember { derivedStateOf { helperWageText.toDoubleOrNull() ?: 0.0 } }
    val miscExp by remember { derivedStateOf { miscText.toDoubleOrNull() ?: 0.0 } }

    val totalDirectExpenses by remember {
        derivedStateOf { fuelExp + evChargingExp + tollExp + maintExp + fineExp + helperExp + miscExp }
    }

    // Auto-calculated Driver Wage
    val autoDriverWage by remember {
        derivedStateOf {
            onCalculateWage(
                selectedVehicle.salaryType,
                selectedVehicle.salaryRate,
                grossIncome,
                totalDirectExpenses
            )
        }
    }

    val finalDriverWage by remember {
        derivedStateOf {
            if (customWageOverride) {
                customWageText.toDoubleOrNull() ?: 0.0
            } else {
                autoDriverWage
            }
        }
    }

    val driverPaidAmount by remember {
        derivedStateOf {
            if (hasManuallyEditedPaid) {
                driverPaidText.toDoubleOrNull() ?: 0.0
            } else {
                finalDriverWage
            }
        }
    }

    val unpaidDailyWage by remember {
        derivedStateOf {
            (finalDriverWage - driverPaidAmount).coerceAtLeast(0.0)
        }
    }

    val netProfit by remember {
        derivedStateOf {
            grossIncome - totalDirectExpenses - finalDriverWage
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.95f),
        title = {
            Column {
                Text(
                    text = if (initialLog == null) "New Daily Vehicle Entry" else "Edit Daily Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Auto-calculates driver pay, EV/fuel & repair bills",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vehicle Picker Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "${selectedVehicle.regNumber} • ${selectedVehicle.nameOrModel}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Public Vehicle") },
                        leadingIcon = {
                            Icon(
                                imageVector = getVehicleTypeIcon(selectedVehicle.type, selectedVehicle.isElectric),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vehicleDropdownExpanded = true }
                            .testTag("vehicle_selector_dropdown")
                    )
                    DropdownMenu(
                        expanded = vehicleDropdownExpanded,
                        onDismissRequest = { vehicleDropdownExpanded = false }
                    ) {
                        vehicles.forEach { v ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            "${v.regNumber} ${if (v.isElectric) "⚡(EV)" else ""}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${v.nameOrModel} • Driver: ${v.driverName}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(getVehicleTypeIcon(v.type, v.isElectric), contentDescription = null)
                                },
                                onClick = {
                                    selectedVehicle = v
                                    isEvCharging = v.isElectric
                                    vehicleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date and Trips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_log_date")
                    )
                    OutlinedTextField(
                        value = tripsCountText,
                        onValueChange = { tripsCountText = it },
                        label = { Text("Trips / Rounds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("input_log_trips")
                    )
                }

                // Gross Income
                OutlinedTextField(
                    value = grossIncomeText,
                    onValueChange = { grossIncomeText = it },
                    label = { Text("Daily Gross Income (Total Fares Collected)") },
                    prefix = { Text("$currency ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gross_income")
                )

                // EV Charging vs Fuel Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ElectricCar,
                                    contentDescription = null,
                                    tint = if (isEvCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Electric Vehicle (EV) Charging", fontWeight = FontWeight.SemiBold)
                                    Text("Track charging units (kWh) and cost", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Switch(
                                checked = isEvCharging,
                                onCheckedChange = { isEvCharging = it }
                            )
                        }

                        if (isEvCharging) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = evUnitsText,
                                    onValueChange = { evUnitsText = it },
                                    label = { Text("Units Charged (kWh)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = evRateText,
                                    onValueChange = { evRateText = it },
                                    label = { Text("Rate per Unit ($currency)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total EV Charging Bill: ${formatMoney(evChargingExp, currency)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = fuelText,
                                onValueChange = { fuelText = it },
                                label = { Text("Fuel Expense (Diesel / Petrol)") },
                                prefix = { Text("$currency ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Standard Operating Expenses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tollText,
                        onValueChange = { tollText = it },
                        label = { Text("Tolls & Parking") },
                        prefix = { Text("$currency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = helperWageText,
                        onValueChange = { helperWageText = it },
                        label = { Text("Helper / Conductor") },
                        prefix = { Text("$currency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fineText,
                        onValueChange = { fineText = it },
                        label = { Text("Challan / Police Fine") },
                        prefix = { Text("$currency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = miscText,
                        onValueChange = { miscText = it },
                        label = { Text("Food / Misc") },
                        prefix = { Text("$currency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Vehicle Maintenance & Repair Bill Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Vehicle Maintenance & Repair Bill",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        OutlinedTextField(
                            value = maintenanceText,
                            onValueChange = { maintenanceText = it },
                            label = { Text("Total Repair / Service Amount") },
                            prefix = { Text("$currency ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = maintenanceWorkText,
                            onValueChange = { maintenanceWorkText = it },
                            label = { Text("Parts Replaced & Work Done Details") },
                            placeholder = { Text("e.g. Brake pad replaced, Mobil oil 4L, air filter, puncture") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = maintenanceBillNoText,
                                onValueChange = { maintenanceBillNoText = it },
                                label = { Text("Bill / Receipt No.") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = maintenanceVendorText,
                                onValueChange = { maintenanceVendorText = it },
                                label = { Text("Workshop / Garage") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Driver Salary & Payment Calculation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Driver Daily Salary: ${selectedVehicle.driverName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                val formulaText = when (selectedVehicle.salaryType) {
                                    SalaryType.FIXED_DAILY.name -> "Fixed daily: $currency ${selectedVehicle.salaryRate}"
                                    SalaryType.PERCENT_OF_INCOME.name -> "${selectedVehicle.salaryRate}% of gross fares"
                                    SalaryType.PERCENT_OF_NET.name -> "${selectedVehicle.salaryRate}% of net fares"
                                    else -> "Custom"
                                }
                                Text(
                                    text = "Rule: $formulaText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatMoney(finalDriverWage, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = if (hasManuallyEditedPaid) driverPaidText else finalDriverWage.toString(),
                            onValueChange = {
                                hasManuallyEditedPaid = true
                                driverPaidText = it
                            },
                            label = { Text("Amount Cash Handed to Driver Today") },
                            prefix = { Text("$currency ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Unpaid Driver Balance for Today:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatMoney(unpaidDailyWage, currency),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (unpaidDailyWage > 0) ExpenseRed else ProfitGreen
                            )
                        }
                    }
                }

                // Live Summary Calculation Card
                val isProfit = netProfit >= 0
                val netContainerColor = if (isProfit) ProfitGreenContainer else ExpenseRedContainer
                val netTextColor = if (isProfit) ProfitGreen else ExpenseRed

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = netContainerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross Income:", style = MaterialTheme.typography.bodyMedium)
                            Text(formatMoney(grossIncome, currency), fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Operating Expenses (Fuel/EV/Repairs):", style = MaterialTheme.typography.bodyMedium)
                            Text("- ${formatMoney(totalDirectExpenses, currency)}", color = ExpenseRed)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Driver Daily Wage:", style = MaterialTheme.typography.bodyMedium)
                            Text("- ${formatMoney(finalDriverWage, currency)}", color = ExpenseRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isProfit) "Net Daily Profit:" else "Net Daily Loss:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = netTextColor
                            )
                            Text(
                                text = formatMoney(netProfit, currency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = netTextColor
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Route Remarks / Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPaid = if (hasManuallyEditedPaid) {
                        driverPaidText.toDoubleOrNull() ?: 0.0
                    } else finalDriverWage

                    val log = FirestoreDailyLog(
                        id = initialLog?.id ?: "",
                        vehicleId = selectedVehicle.id,
                        vehicleReg = selectedVehicle.regNumber,
                        date = dateText.trim(),
                        grossIncome = grossIncome,
                        fuelExpense = fuelExp,
                        isEvCharging = isEvCharging,
                        evUnitsKwh = evUnits,
                        evRatePerUnit = evRate,
                        evChargingExpense = evChargingExp,
                        tollAndTaxExpense = tollExp,
                        maintenanceExpense = maintExp,
                        maintenanceWorkDetails = maintenanceWorkText.trim(),
                        maintenanceBillNo = maintenanceBillNoText.trim(),
                        maintenanceVendor = maintenanceVendorText.trim(),
                        challanOrFineExpense = fineExp,
                        helperWageExpense = helperExp,
                        foodOrMiscExpense = miscExp,
                        totalOperatingExpenses = totalDirectExpenses,
                        driverWageEarned = finalDriverWage,
                        driverPaidAmount = finalPaid,
                        isDriverFullyPaid = finalPaid >= finalDriverWage,
                        netDailyProfit = netProfit,
                        tripsOrRounds = tripsCountText.toLongOrNull() ?: 1L,
                        notes = notesText.trim()
                    )
                    onSave(log)
                },
                modifier = Modifier.testTag("save_daily_log_button")
            ) {
                Text("Save Daily Log")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
