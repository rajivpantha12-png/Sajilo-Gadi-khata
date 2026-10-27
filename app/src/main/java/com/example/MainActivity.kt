package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import com.example.ui.components.DownloadShareDialog
import com.example.ui.components.FatherQuickEntryDialog
import com.example.ui.components.QuickEntryType
import com.example.ui.components.UpdateDialog
import com.example.ui.util.AppLanguage
import com.example.ui.util.NepaliStrings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.firestore.FirestoreDailyLog
import com.example.data.firestore.FirestoreVehicle
import com.example.ui.auth.AuthManager
import com.example.ui.components.DailyEntryDialog
import com.example.ui.components.VehicleEditDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DriverScreen
import com.example.ui.screens.MonthlyReportsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.VehiclesScreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FleetViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

enum class NavTab(val icon: ImageVector, val tag: String) {
    DAILY_LEDGER(Icons.Default.Today, "tab_daily"),
    VEHICLES(Icons.Default.DirectionsBus, "tab_vehicles"),
    DRIVERS(Icons.Default.Person, "tab_drivers"),
    REPORTS(Icons.Default.Assessment, "tab_reports");

    fun getTitle(lang: AppLanguage): String = when (this) {
        DAILY_LEDGER -> NepaliStrings.tabDailyLedger(lang)
        VEHICLES -> NepaliStrings.tabVehicles(lang)
        DRIVERS -> NepaliStrings.tabDrivers(lang)
        REPORTS -> NepaliStrings.tabReports(lang)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainRootNavigation()
                }
            }
        }
    }
}

@Composable
fun MainRootNavigation() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }
    var showSignInModal by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            val credentialManager = CredentialManager.create(context)
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                scope = coroutineScope,
                onAuthSuccess = { user ->
                    currentUser = user
                },
                onUnauthenticated = {
                    // Operate seamlessly in local offline mode
                }
            )
        }
    }

    FleetLedgerApp(
        currentUser = currentUser,
        onSignInRequested = {
            showSignInModal = true
        },
        onSignOut = {
            currentUser = null
        }
    )

    if (showSignInModal && currentUser == null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSignInModal = false }) {
            SignInScreen(
                onSignInSuccess = {
                    currentUser = Firebase.auth.currentUser
                    showSignInModal = false
                },
                onContinueOffline = {
                    showSignInModal = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetLedgerApp(
    currentUser: FirebaseUser?,
    onSignInRequested: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: FleetViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavTab.DAILY_LEDGER) }

    val vehicles by viewModel.allVehicles.collectAsState()
    val allLogs by viewModel.allDailyLogs.collectAsState()
    val allPayouts by viewModel.allDriverPayouts.collectAsState()
    val allInstallments by viewModel.allInstallments.collectAsState()
    val todaySummary by viewModel.todaySummary.collectAsState()
    val monthlyReport by viewModel.monthlyReport.collectAsState()
    val driverSummaries by viewModel.driverSummaries.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedVehicleFilter by viewModel.selectedVehicleFilter.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    val isFatherMode by viewModel.isFatherMode.collectAsState()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val showDownloadShareDialog by viewModel.showDownloadShareDialog.collectAsState()

    // Dialog States
    var showDailyLogDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<FirestoreDailyLog?>(null) }
    var defaultVehicleForLog by remember { mutableStateOf<String?>(null) }

    var showFatherQuickIncome by remember { mutableStateOf(false) }
    var showFatherQuickExpense by remember { mutableStateOf(false) }

    var showVehicleDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<FirestoreVehicle?>(null) }

    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = currentTab != NavTab.DAILY_LEDGER) {
        currentTab = NavTab.DAILY_LEDGER
    }

    var forceDesktopPreview by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktopOrTablet = maxWidth >= 640.dp || forceDesktopPreview

        if (isDesktopOrTablet) {
            // Desktop / Tablet Canonical Layout with Side NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsBus,
                                    contentDescription = "FleetLedger Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "FleetLedger",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "DESKTOP",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    NavTab.values().forEach { tab ->
                        NavigationRailItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.getTitle(language)) },
                            label = { Text(tab.getTitle(language)) },
                            modifier = Modifier.testTag("rail_${tab.tag}")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    // Mode Switcher & Tools at bottom of Rail
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { currencyMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currency,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Toggle back to Mobile View button
                        IconButton(
                            onClick = { forceDesktopPreview = !forceDesktopPreview },
                            modifier = Modifier.testTag("toggle_mobile_view_btn")
                        ) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = "Switch to Mobile View",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        IconButton(onClick = { accountMenuExpanded = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Main Content Pane for Desktop/Tablet
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (language == AppLanguage.NEPALI) "सजिलो गाडी खाता • डेस्कटप साइट" else "Sajilo Gadi Khata • Desktop Site",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                "WIDESCREEN",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (currentUser != null) (currentUser.email ?: "Cloud Sync Active") else "Local Fleet Mode (Offline & Cloud Ready)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                if (currentUser == null) {
                                    Button(
                                        onClick = onSignInRequested,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(end = 8.dp).testTag("desktop_sign_in_btn")
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.NEPALI) "साइन-इन (Google)" else "Sign in with Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (isUpdateAvailable) {
                                    Button(
                                        onClick = { viewModel.setShowUpdateDialog(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(end = 8.dp).testTag("desktop_update_btn")
                                    ) {
                                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.NEPALI) "नयाँ अपडेट" else "Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Button(
                                    onClick = { viewModel.setShowDownloadShareDialog(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp).testTag("desktop_qr_download_btn")
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (language == AppLanguage.NEPALI) "QR / डाउनलोड" else "QR / Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.setLanguage(if (language == AppLanguage.NEPALI) AppLanguage.ENGLISH else AppLanguage.NEPALI)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (language == AppLanguage.NEPALI) "नेपाली" else "English", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { forceDesktopPreview = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (language == AppLanguage.NEPALI) "मोबाइल दृश्य" else "Mobile View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        TabContent(
                            currentTab = currentTab,
                            todaySummary = todaySummary,
                            vehicles = vehicles,
                            allLogs = allLogs,
                            allPayouts = allPayouts,
                            allInstallments = allInstallments,
                            monthlyReport = monthlyReport,
                            driverSummaries = driverSummaries,
                            selectedDate = selectedDate,
                            selectedMonth = selectedMonth,
                            selectedVehicleFilter = selectedVehicleFilter,
                            currency = currency,
                            language = language,
                            isFatherMode = isFatherMode,
                            currentUser = currentUser,
                            isUpdateAvailable = isUpdateAvailable,
                            viewModel = viewModel,
                            isDesktop = true,
                            onAddLog = { vehicleId ->
                                editingLog = null
                                defaultVehicleForLog = vehicleId
                                showDailyLogDialog = true
                            },
                            onEditLog = { log ->
                                editingLog = log
                                defaultVehicleForLog = log.vehicleId
                                showDailyLogDialog = true
                            },
                            onAddVehicle = {
                                editingVehicle = null
                                showVehicleDialog = true
                            },
                            onEditVehicle = { v ->
                                editingVehicle = v
                                showVehicleDialog = true
                            },
                            onSignInClick = onSignInRequested,
                            onShowDownloadShareClick = { viewModel.setShowDownloadShareDialog(true) },
                            onShowUpdateClick = { viewModel.setShowUpdateDialog(true) },
                            onToggleFatherMode = { viewModel.setFatherMode(it) },
                            onQuickIncomeClick = { showFatherQuickIncome = true },
                            onQuickExpenseClick = { showFatherQuickExpense = true }
                        )
                    }
                }
            }
        } else {
            // Standard Handheld Mobile Layout with Bottom NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "सजिलो गाडी खाता" else "Sajilo Gadi Khata",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentUser != null) (currentUser.email ?: "Cloud Synced") else (if (language == AppLanguage.NEPALI) "लोकल खाता • ☁️ बाट सिङ्क गर्नुहोस्" else "Local Fleet Mode • Tap ☁️ to Sync"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                IconButton(onClick = { accountMenuExpanded = true }) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "Account Profile",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        },
                        actions = {
                            if (currentUser == null) {
                                Button(
                                    onClick = onSignInRequested,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.padding(end = 4.dp).testTag("mobile_sign_in_btn")
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(if (language == AppLanguage.NEPALI) "साइन-इन" else "Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isUpdateAvailable) {
                                IconButton(
                                    onClick = { viewModel.setShowUpdateDialog(true) },
                                    modifier = Modifier.testTag("mobile_update_btn")
                                ) {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = "Update Available", tint = MaterialTheme.colorScheme.tertiary)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.setShowDownloadShareDialog(true) },
                                modifier = Modifier.testTag("mobile_qr_download_btn")
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = "QR Download & Share", tint = MaterialTheme.colorScheme.primary)
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setLanguage(if (language == AppLanguage.NEPALI) AppLanguage.ENGLISH else AppLanguage.NEPALI)
                                    }
                                    .padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "नेपा" else "EN",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Desktop Site Preview Toggle Button
                            IconButton(
                                onClick = { forceDesktopPreview = true },
                                modifier = Modifier.testTag("toggle_desktop_preview_btn")
                            ) {
                                Icon(
                                    Icons.Default.DesktopWindows,
                                    contentDescription = "Desktop Site Preview",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { currencyMenuExpanded = true }
                                        .padding(end = 4.dp)
                                        .testTag("currency_switcher_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currency,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("main_navigation_bar")
                    ) {
                        NavTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.getTitle(language)) },
                                label = { Text(tab.getTitle(language)) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabContent(
                        currentTab = currentTab,
                        todaySummary = todaySummary,
                        vehicles = vehicles,
                        allLogs = allLogs,
                        allPayouts = allPayouts,
                        allInstallments = allInstallments,
                        monthlyReport = monthlyReport,
                        driverSummaries = driverSummaries,
                        selectedDate = selectedDate,
                        selectedMonth = selectedMonth,
                        selectedVehicleFilter = selectedVehicleFilter,
                        currency = currency,
                        language = language,
                        isFatherMode = isFatherMode,
                        currentUser = currentUser,
                        isUpdateAvailable = isUpdateAvailable,
                        viewModel = viewModel,
                        isDesktop = false,
                        onAddLog = { vehicleId ->
                            editingLog = null
                            defaultVehicleForLog = vehicleId
                            showDailyLogDialog = true
                        },
                        onEditLog = { log ->
                            editingLog = log
                            defaultVehicleForLog = log.vehicleId
                            showDailyLogDialog = true
                        },
                        onAddVehicle = {
                            editingVehicle = null
                            showVehicleDialog = true
                        },
                        onEditVehicle = { v ->
                            editingVehicle = v
                            showVehicleDialog = true
                        },
                        onSignInClick = onSignInRequested,
                        onShowDownloadShareClick = { viewModel.setShowDownloadShareDialog(true) },
                        onShowUpdateClick = { viewModel.setShowUpdateDialog(true) },
                        onToggleFatherMode = { viewModel.setFatherMode(it) },
                        onQuickIncomeClick = { showFatherQuickIncome = true },
                        onQuickExpenseClick = { showFatherQuickExpense = true }
                    )
                }
            }
        }

        // Dropdown menus
        DropdownMenu(
            expanded = accountMenuExpanded,
            onDismissRequest = { accountMenuExpanded = false }
        ) {
            if (currentUser != null) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(currentUser.displayName ?: "Fleet Owner", fontWeight = FontWeight.Bold)
                            Text(currentUser.email ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("Sign Out", color = ExpenseRed) },
                    leadingIcon = {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = ExpenseRed)
                    },
                    onClick = {
                        accountMenuExpanded = false
                        viewModel.signOut(context) {
                            onSignOut()
                        }
                    }
                )
            } else {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("Fleet Owner", fontWeight = FontWeight.Bold)
                            Text("Operating in Local Fleet Mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("Sign In with Google (Cloud Sync)") },
                    leadingIcon = {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    onClick = {
                        accountMenuExpanded = false
                        onSignInRequested()
                    }
                )
            }
        }

        DropdownMenu(
            expanded = currencyMenuExpanded,
            onDismissRequest = { currencyMenuExpanded = false }
        ) {
            listOf("Rs.", "₹", "$", "£", "€", "৳", "KSh").forEach { cur ->
                DropdownMenuItem(
                    text = { Text("Currency: $cur") },
                    onClick = {
                        viewModel.setCurrency(cur)
                        currencyMenuExpanded = false
                    }
                )
            }
        }
    }

    // Daily Log Entry Dialog
    if (showDailyLogDialog) {
        DailyEntryDialog(
            vehicles = vehicles,
            initialLog = editingLog,
            defaultVehicleId = defaultVehicleForLog,
            defaultDate = selectedDate,
            currency = currency,
            onCalculateWage = { salaryType, rate, gross, expenses ->
                viewModel.calculateDriverWage(salaryType, rate, gross, expenses)
            },
            onDismiss = {
                showDailyLogDialog = false
                editingLog = null
            },
            onSave = { log ->
                viewModel.saveDailyLog(log)
                showDailyLogDialog = false
                editingLog = null
            }
        )
    }

    // Vehicle Add/Edit Dialog
    if (showVehicleDialog) {
        VehicleEditDialog(
            vehicle = editingVehicle,
            currency = currency,
            onDismiss = {
                showVehicleDialog = false
                editingVehicle = null
            },
            onSave = { vehicle ->
                viewModel.saveVehicle(vehicle)
                showVehicleDialog = false
                editingVehicle = null
            },
            onDelete = { vehicleId ->
                viewModel.deleteVehicle(vehicleId)
                showVehicleDialog = false
                editingVehicle = null
            }
        )
    }

    // QR Code Download & Share Dialog
    if (showDownloadShareDialog) {
        DownloadShareDialog(
            language = language,
            onDismissRequest = { viewModel.setShowDownloadShareDialog(false) }
        )
    }

    // Auto-Update Dialog
    if (showUpdateDialog) {
        UpdateDialog(
            language = language,
            currentVersion = "1.0",
            newVersion = "1.1",
            onDismissRequest = { viewModel.setShowUpdateDialog(false) }
        )
    }

    // Father's Senior Easy Quick Entry Dialog (Income)
    if (showFatherQuickIncome) {
        FatherQuickEntryDialog(
            entryType = QuickEntryType.INCOME,
            vehicles = vehicles,
            language = language,
            currency = currency,
            onDismissRequest = { showFatherQuickIncome = false },
            onSaveEntry = { log ->
                viewModel.saveDailyLog(log)
                showFatherQuickIncome = false
            }
        )
    }

    // Father's Senior Easy Quick Entry Dialog (Expense)
    if (showFatherQuickExpense) {
        FatherQuickEntryDialog(
            entryType = QuickEntryType.EXPENSE,
            vehicles = vehicles,
            language = language,
            currency = currency,
            onDismissRequest = { showFatherQuickExpense = false },
            onSaveEntry = { log ->
                viewModel.saveDailyLog(log)
                showFatherQuickExpense = false
            }
        )
    }
}

@Composable
fun TabContent(
    currentTab: NavTab,
    todaySummary: com.example.ui.viewmodel.DailySummary,
    vehicles: List<FirestoreVehicle>,
    allLogs: List<FirestoreDailyLog>,
    allPayouts: List<com.example.data.firestore.FirestoreDriverPayout>,
    allInstallments: List<com.example.data.firestore.FirestoreBankInstallment>,
    monthlyReport: com.example.ui.viewmodel.MonthlyReport,
    driverSummaries: List<com.example.ui.viewmodel.DriverSummary>,
    selectedDate: String,
    selectedMonth: String,
    selectedVehicleFilter: String?,
    currency: String,
    language: AppLanguage,
    isFatherMode: Boolean,
    currentUser: FirebaseUser?,
    isUpdateAvailable: Boolean,
    viewModel: FleetViewModel,
    isDesktop: Boolean = false,
    onAddLog: (String?) -> Unit,
    onEditLog: (FirestoreDailyLog) -> Unit,
    onAddVehicle: () -> Unit,
    onEditVehicle: (FirestoreVehicle) -> Unit,
    onSignInClick: () -> Unit,
    onShowDownloadShareClick: () -> Unit,
    onShowUpdateClick: () -> Unit,
    onToggleFatherMode: (Boolean) -> Unit,
    onQuickIncomeClick: () -> Unit,
    onQuickExpenseClick: () -> Unit
) {
    when (currentTab) {
        NavTab.DAILY_LEDGER -> {
            DashboardScreen(
                summary = todaySummary,
                vehicles = vehicles,
                allLogs = allLogs,
                selectedDate = selectedDate,
                selectedVehicleId = selectedVehicleFilter,
                currency = currency,
                language = language,
                isFatherMode = isFatherMode,
                isSignedIn = currentUser != null,
                userEmail = currentUser?.email,
                isUpdateAvailable = isUpdateAvailable,
                onSelectVehicleFilter = { viewModel.setVehicleFilter(it) },
                onPreviousDay = { viewModel.shiftDate(-1) },
                onNextDay = { viewModel.shiftDate(1) },
                onAddLogClick = onAddLog,
                onEditLogClick = onEditLog,
                onDeleteLogClick = { log -> viewModel.deleteDailyLog(log.id) },
                onSignInClick = onSignInClick,
                onShowDownloadShareClick = onShowDownloadShareClick,
                onShowUpdateClick = onShowUpdateClick,
                onToggleFatherMode = onToggleFatherMode,
                onQuickIncomeClick = onQuickIncomeClick,
                onQuickExpenseClick = onQuickExpenseClick,
                isDesktop = isDesktop
            )
        }

        NavTab.VEHICLES -> {
            VehiclesScreen(
                vehicles = vehicles,
                currency = currency,
                onAddVehicle = onAddVehicle,
                onEditVehicle = onEditVehicle,
                onLogShiftForVehicle = onAddLog,
                isDesktop = isDesktop
            )
        }

        NavTab.DRIVERS -> {
            DriverScreen(
                driverSummaries = driverSummaries,
                vehicles = vehicles,
                allPayouts = allPayouts,
                currency = currency,
                onRecordPayout = { vId, dName, amt, date, mode, notes ->
                    viewModel.recordDriverPayout(vId, dName, amt, date, mode, notes)
                },
                onDeletePayout = { payoutId ->
                    viewModel.deleteDriverPayout(payoutId)
                },
                isDesktop = isDesktop
            )
        }

        NavTab.REPORTS -> {
            MonthlyReportsScreen(
                report = monthlyReport,
                vehicles = vehicles,
                installments = allInstallments,
                selectedMonth = selectedMonth,
                selectedVehicleId = selectedVehicleFilter,
                currency = currency,
                onSelectVehicleFilter = { viewModel.setVehicleFilter(it) },
                onPreviousMonth = { viewModel.shiftMonth(-1) },
                onNextMonth = { viewModel.shiftMonth(1) },
                onMarkEmiPaid = { inst, amt, date, ref ->
                    viewModel.markInstallmentAsPaid(inst, amt, date, ref)
                },
                onGetBackupJson = {
                    viewModel.getConsolidatedBackupJson()
                },
                isDesktop = isDesktop
            )
        }
    }
}
