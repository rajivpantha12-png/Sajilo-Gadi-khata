package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firestore.FirestoreDailyLog
import com.example.data.firestore.FirestoreVehicle
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QuickEntryType {
    INCOME,
    EXPENSE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FatherQuickEntryDialog(
    entryType: QuickEntryType,
    vehicles: List<FirestoreVehicle>,
    language: AppLanguage,
    currency: String,
    onDismissRequest: () -> Unit,
    onSaveEntry: (FirestoreDailyLog) -> Unit
) {
    var selectedVehicleId by remember {
        mutableStateOf(vehicles.firstOrNull()?.id ?: "")
    }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(if (entryType == QuickEntryType.INCOME) "भाडा कमाइ" else "डिजेल / पेट्रोल")
    }
    var remarksText by remember { mutableStateOf("") }
    var tripsCount by remember { mutableStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val todayFormatted = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.96f)
                .testTag("father_quick_entry_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (entryType == QuickEntryType.INCOME) ProfitGreen.copy(alpha = 0.15f)
                                    else ExpenseRed.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (entryType == QuickEntryType.INCOME) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                contentDescription = null,
                                tint = if (entryType == QuickEntryType.INCOME) ProfitGreen else ExpenseRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (entryType == QuickEntryType.INCOME) "आजको कमाइ थप्नुहोस्" else "आजको खर्च लेख्नुहोस्",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (entryType == QuickEntryType.INCOME) ProfitGreen else ExpenseRed
                            )
                            Text(
                                text = "सजिलो हिसाब • मिति: $todayFormatted",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 1: Vehicle Selector (Big Cards)
                Text(
                    text = "१. कुन गाडीको हिसाब हो? (गाडी रोज्नुहोस्)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vehicles.forEach { v ->
                        val isSelected = v.id == selectedVehicleId
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVehicleId = v.id }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when {
                                    v.isElectric || v.type.contains("EV") -> Icons.Default.ElectricCar
                                    v.type == "TAXI" -> Icons.Default.LocalTaxi
                                    else -> Icons.Default.DirectionsBus
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = v.regNumber,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${v.nameOrModel} • ${v.routeOrArea}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 2: Amount Input (Huge Font)
                Text(
                    text = "२. कति रकम हो? ($currency मा लेख्नुहोस्)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                        }
                    },
                    label = { Text("रकम लेख्नुहोस् (जस्तै: 5000)", fontSize = 16.sp) },
                    prefix = {
                        Text(
                            text = "$currency ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entryType == QuickEntryType.INCOME) ProfitGreen else ExpenseRed
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (entryType == QuickEntryType.INCOME) ProfitGreen else ExpenseRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("father_input_amount"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Quick Amount Shortcuts (+500, +1000, +2000, +5000)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500, 1000, 2000, 5000).forEach { quickVal ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val cur = amountText.toDoubleOrNull() ?: 0.0
                                    amountText = (cur + quickVal).toInt().toString()
                                }
                        ) {
                            Text(
                                text = "+$quickVal",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 3: Category (Only for Expense) or Trips (for Income)
                if (entryType == QuickEntryType.EXPENSE) {
                    Text(
                        text = "३. खर्च केमा भयो?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf(
                        "डिजेल / पेट्रोल",
                        "विद्युतीय चार्ज (EV)",
                        "गाडी मर्मत",
                        "सडक कर / टोल",
                        "चालक खाना/भत्ता",
                        "ट्राफिक चिट",
                        "अन्य खर्च"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                } else {
                    // For Income: Track trips
                    Text(
                        text = "३. आज कति ट्रिप भयो?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(1, 2, 3, 4, 5, 6).forEach { t ->
                            val isSel = tripsCount == t
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { tripsCount = t }
                                    .size(46.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$t",
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Notes
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("कैफियत / टिप्पणी (ऐच्छिक)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step 4: Big Confirm & Save Button
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "कृपया सहि रकम लेख्नुहोस्।"
                            return@Button
                        }
                        if (selectedVehicleId.isBlank()) {
                            errorMessage = "कृपया गाडी रोज्नुहोस्।"
                            return@Button
                        }

                        val targetVehicle = vehicles.firstOrNull { it.id == selectedVehicleId }
                        val vehicleReg = targetVehicle?.regNumber ?: ""

                        val logToSave = if (entryType == QuickEntryType.INCOME) {
                            FirestoreDailyLog(
                                date = todayFormatted,
                                vehicleId = selectedVehicleId,
                                vehicleReg = vehicleReg,
                                tripsOrRounds = tripsCount.toLong(),
                                grossIncome = amount,
                                netDailyProfit = amount,
                                notes = if (remarksText.isNotBlank()) remarksText else "दैनिक कमाइ"
                            )
                        } else {
                            val fuelExpense = if (selectedCategory.contains("डिजेल") || selectedCategory.contains("पेट्रोल") || selectedCategory.contains("इन्धन")) amount else 0.0
                            val evExpense = if (selectedCategory.contains("विद्युतीय") || selectedCategory.contains("EV")) amount else 0.0
                            val maintenanceExpense = if (selectedCategory.contains("मर्मत")) amount else 0.0
                            val tollExpense = if (selectedCategory.contains("सडक") || selectedCategory.contains("टोल")) amount else 0.0
                            val foodExpense = if (selectedCategory.contains("खाना") || selectedCategory.contains("भत्ता")) amount else 0.0
                            val fineExpense = if (selectedCategory.contains("ट्राफिक") || selectedCategory.contains("चिट")) amount else 0.0

                            FirestoreDailyLog(
                                date = todayFormatted,
                                vehicleId = selectedVehicleId,
                                vehicleReg = vehicleReg,
                                grossIncome = 0.0,
                                fuelExpense = fuelExpense,
                                isEvCharging = evExpense > 0.0,
                                evChargingExpense = evExpense,
                                maintenanceExpense = maintenanceExpense,
                                tollAndTaxExpense = tollExpense,
                                foodOrMiscExpense = foodExpense,
                                challanOrFineExpense = fineExpense,
                                totalOperatingExpenses = amount,
                                netDailyProfit = -amount,
                                notes = if (remarksText.isNotBlank()) remarksText else selectedCategory
                            )
                        }

                        onSaveEntry(logToSave)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("father_save_entry_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (entryType == QuickEntryType.INCOME) ProfitGreen else ExpenseRed
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "सेभ गर्नुहोस् (Save Entry)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
