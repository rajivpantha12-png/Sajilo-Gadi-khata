package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.firestore.FirestoreVehicle
import com.example.data.model.SalaryType
import com.example.ui.theme.ExpenseRed

val VEHICLE_TYPE_OPTIONS = listOf(
    "BUS" to "Passenger Bus",
    "EV_BUS" to "Electric Bus (EV)",
    "MICROBUS" to "Microbus / Van",
    "EV_VAN" to "Electric Micro / Van (EV)",
    "TAXI" to "Taxi / City Cab",
    "AUTO" to "Auto-Rickshaw / Tuk-Tuk",
    "EV_AUTO" to "Electric Auto / Safari (EV)",
    "TRUCK" to "Truck / Pickup",
    "OTHER" to "Other Vehicle"
)

@Composable
fun VehicleEditDialog(
    vehicle: FirestoreVehicle? = null,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (FirestoreVehicle) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var regNumber by remember { mutableStateOf(vehicle?.regNumber ?: "") }
    var nameOrModel by remember { mutableStateOf(vehicle?.nameOrModel ?: "") }
    var selectedType by remember { mutableStateOf(vehicle?.type ?: "BUS") }
    var isElectric by remember { mutableStateOf(vehicle?.isElectric ?: selectedType.startsWith("EV_")) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var routeOrArea by remember { mutableStateOf(vehicle?.routeOrArea ?: "") }

    // Driver details
    var driverName by remember { mutableStateOf(vehicle?.driverName ?: "") }
    var driverPhone by remember { mutableStateOf(vehicle?.driverPhone ?: "") }
    var selectedSalaryType by remember { mutableStateOf(vehicle?.salaryType ?: "FIXED_DAILY") }
    var salaryRateText by remember { mutableStateOf(vehicle?.salaryRate?.toString() ?: "1200") }

    // Bank loan EMI details
    var bankName by remember { mutableStateOf(vehicle?.bankName ?: "") }
    var monthlyEmiText by remember { mutableStateOf(if (vehicle != null && vehicle.monthlyEmi > 0) vehicle.monthlyEmi.toString() else "") }
    var emiDueDayText by remember { mutableStateOf(vehicle?.emiDueDay?.toString() ?: "10") }
    var totalLoanText by remember { mutableStateOf(if (vehicle != null && vehicle.totalLoanAmount > 0) vehicle.totalLoanAmount.toString() else "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.95f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (vehicle == null) "Add Fleet Vehicle" else "Edit Vehicle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (vehicle != null && onDelete != null && vehicle.id.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onDelete(vehicle.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        modifier = Modifier.testTag("delete_vehicle_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.padding(end = 4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Vehicle Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = regNumber,
                    onValueChange = {
                        regNumber = it
                        isError = false
                    },
                    label = { Text("Registration Number (e.g. BA-2-KHA 5521)*") },
                    isError = isError && regNumber.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reg_number")
                )

                OutlinedTextField(
                    value = nameOrModel,
                    onValueChange = { nameOrModel = it },
                    label = { Text("Model / Fleet Name (e.g. Photon EV / City Bus)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Vehicle Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = VEHICLE_TYPE_OPTIONS.find { it.first == selectedType }?.second ?: selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle Type") },
                        leadingIcon = {
                            Icon(getVehicleTypeIcon(selectedType, isElectric), contentDescription = null)
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        VEHICLE_TYPE_OPTIONS.forEach { (typeKey, typeLabel) ->
                            DropdownMenuItem(
                                text = { Text(typeLabel) },
                                leadingIcon = {
                                    Icon(getVehicleTypeIcon(typeKey, typeKey.startsWith("EV_")), contentDescription = null)
                                },
                                onClick = {
                                    selectedType = typeKey
                                    if (typeKey.startsWith("EV_")) isElectric = true
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // EV Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Electric Vehicle (EV)", fontWeight = FontWeight.SemiBold)
                    }
                    Switch(checked = isElectric, onCheckedChange = { isElectric = it })
                }

                OutlinedTextField(
                    value = routeOrArea,
                    onValueChange = { routeOrArea = it },
                    label = { Text("Assigned Route / Operating Area") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()

                // Driver & Salary
                Text(
                    text = "Driver & Daily Salary Rules",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = driverPhone,
                    onValueChange = { driverPhone = it },
                    label = { Text("Driver Contact Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Daily Salary Calculation Formula:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        SalaryType.values().forEach { st ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSalaryType = st.name }
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSalaryType == st.name,
                                    onClick = { selectedSalaryType = st.name }
                                )
                                Text(st.displayName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        val rateLabel = when (selectedSalaryType) {
                            "FIXED_DAILY" -> "Fixed Daily Wage ($currency)"
                            "PERCENT_OF_INCOME" -> "Commission Rate (% of Gross Fares)"
                            "PERCENT_OF_NET" -> "Commission Rate (% of Net Fares)"
                            else -> "Rate"
                        }

                        OutlinedTextField(
                            value = salaryRateText,
                            onValueChange = { salaryRateText = it },
                            label = { Text(rateLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()

                // Bank Loan / EMI
                Text(
                    text = "Bank Loan / Installment (EMI) Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Financing Bank / Institution Name") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = monthlyEmiText,
                        onValueChange = { monthlyEmiText = it },
                        label = { Text("Monthly EMI Amount") },
                        prefix = { Text("$currency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = emiDueDayText,
                        onValueChange = { emiDueDayText = it },
                        label = { Text("Due Day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                OutlinedTextField(
                    value = totalLoanText,
                    onValueChange = { totalLoanText = it },
                    label = { Text("Total Financed Loan Amount (Optional)") },
                    prefix = { Text("$currency ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regNumber.isBlank()) {
                        isError = true
                        return@Button
                    }
                    val updated = FirestoreVehicle(
                        id = vehicle?.id ?: "",
                        regNumber = regNumber.trim().uppercase(),
                        nameOrModel = nameOrModel.trim(),
                        type = selectedType,
                        isElectric = isElectric,
                        routeOrArea = routeOrArea.trim(),
                        driverName = driverName.trim(),
                        driverPhone = driverPhone.trim(),
                        salaryType = selectedSalaryType,
                        salaryRate = salaryRateText.toDoubleOrNull() ?: 1200.0,
                        bankName = bankName.trim(),
                        monthlyEmi = monthlyEmiText.toDoubleOrNull() ?: 0.0,
                        emiDueDay = (emiDueDayText.toLongOrNull() ?: 10L).coerceIn(1L, 31L),
                        totalLoanAmount = totalLoanText.toDoubleOrNull() ?: 0.0,
                        isActive = true,
                        createdAt = vehicle?.createdAt
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_vehicle_button")
            ) {
                Text(if (vehicle == null) "Add Vehicle" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
