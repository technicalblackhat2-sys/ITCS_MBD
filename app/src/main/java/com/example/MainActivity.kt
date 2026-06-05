package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: InstituteViewModel = viewModel()
                
                // Root Container Scaffold
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        AppNavigationEntry(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigationEntry(viewModel: InstituteViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    // Route views safely
    AnimatedContent(
        targetState = activeScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            ActiveScreen.BiometricLock -> {
                BiometricLockScreen(viewModel)
            }
            ActiveScreen.RoleSelector -> {
                RoleSelectorScreen(viewModel)
            }
            ActiveScreen.AdminAdmissions,
            ActiveScreen.AdminStudentsList,
            ActiveScreen.AdminSchedules,
            ActiveScreen.TeacherAttendance,
            ActiveScreen.TeacherGrades,
            ActiveScreen.StudentPortal,
            ActiveScreen.ParentPortal,
            ActiveScreen.MessagingPortal,
            ActiveScreen.SyncConflictCenter -> {
                MainLayoutContainer(viewModel, screen)
            }
        }
    }
}

// 1. BIOMETRIC SECURITY SCREEN
@Composable
fun BiometricLockScreen(viewModel: InstituteViewModel) {
    var feedbackText by remember { mutableStateOf("Hold your finger on the sensor below to authenticate") }
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secured",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ITCS Secured Vault",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Biometric Verification Required",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulse fingerprint visual
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(if (isScanning) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable {
                        scope.launch {
                            isScanning = true
                            feedbackText = "Recognizing biometric characteristics..."
                            delay(1500)
                            isScanning = false
                            feedbackText = "Authenticated successfully!"
                            delay(400)
                            viewModel.authenticateWithBiometrics(true)
                        }
                    }
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Scan Finger",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = feedbackText,
                fontSize = 14.sp,
                color = if (isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = { viewModel.authenticateWithBiometrics(true) }, // Bypass option
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Use Passcode (Bypass)", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// 2. ROLE CHANGER SCREEN
@Composable
fun RoleSelectorScreen(viewModel: InstituteViewModel) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Scaped info matching website style
            Text(
                text = "IT COMPUTER STUDIES",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = "COMPUTER EDUCATION PORTAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            Text(
                text = "Select Login Workspace",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val rolesList = listOf(
                Triple("Admin", "Institute Director", Icons.Default.Settings),
                Triple("Teacher", "Faculty instructor panel", Icons.Default.School),
                Triple("Student", "Student portal & courses", Icons.Default.Person),
                Triple("Parent", "Parent performance tracker", Icons.Default.Assessment)
            )

            rolesList.forEach { (role, desc, icon) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { viewModel.selectRole(role) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = role,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "$role Space",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric Option Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleBiometricSetup() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Require Biometric Login",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometricSetup() }
                )
            }
        }
    }
}

// 3. MASTER INTEGRATED SCAFFOLD CONTAINER
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayoutContainer(viewModel: InstituteViewModel, activeScreen: ActiveScreen) {
    val currentRole by viewModel.currentRole.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pendingSyncs by viewModel.pendingSyncs.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    
    // Warn dialog states
    var showAlertDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Custom Top Navigation Header
        Surface(
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
            modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ITCS Lucknow",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                                contentDescription = "Online",
                                tint = if (isOnline) Color(0xFF10B981) else Color(0xFFB3261E),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Cloud Active" else "Offline Cache Mode",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Offline Sync Badge Tracker
                        if (pendingSyncs.isNotEmpty()) {
                            Badge(
                                containerColor = Color(0xFFFFDAD6),
                                contentColor = Color(0xFF410002),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { viewModel.setScreen(ActiveScreen.SyncConflictCenter) }
                            ) {
                                Text("${pendingSyncs.size} Unsaved", modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }

                        // Conflict center quick link
                        if (conflicts.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setScreen(ActiveScreen.SyncConflictCenter) }) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Conflict",
                                    tint = Color(0xFFB3261E)
                                )
                            }
                        }

                        // Connectivity simulation toggle switch
                        IconButton(onClick = { viewModel.toggleInternetConnectivity() }) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Refresh else Icons.Default.CloudOff,
                                contentDescription = "Toggle Network Connection",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Real-time alarm board toggle triggers
                        IconButton(onClick = { showAlertDropdown = !showAlertDropdown }) {
                            BadgedBox(badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFFB3261E)) {
                                        Text("${notifications.size}")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alert Board",
                                    tint = if (notifications.any { it.type == NotificationType.Tuition }) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Exit profile
                        IconButton(onClick = { viewModel.setScreen(ActiveScreen.RoleSelector) }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Switch profile",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Show Current role banner
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Viewing as: $currentRole",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Website Info: itcsedu.in",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Real-Time Warning Alert Tray
        if (showAlertDropdown && notifications.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚠️ Urgent Warnings & System Reminders",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    notifications.forEach { notif ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (notif.type == NotificationType.Tuition) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = notif.message,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissNotification(notif.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Resolve alert",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Viewport Screen Contents
        Box(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.background)) {
            when (activeScreen) {
                ActiveScreen.AdminAdmissions -> AdminAdmissionsScreen(viewModel)
                ActiveScreen.AdminStudentsList -> AdminStudentsScreen(viewModel)
                ActiveScreen.AdminSchedules -> AdminSchedulesScreen(viewModel)
                ActiveScreen.TeacherAttendance -> TeacherAttendanceScreen(viewModel)
                ActiveScreen.TeacherGrades -> TeacherGradesScreen(viewModel)
                ActiveScreen.StudentPortal -> StudentDashboardScreen(viewModel)
                ActiveScreen.ParentPortal -> ParentDashboardScreen(viewModel)
                ActiveScreen.MessagingPortal -> MessagingPortalScreen(viewModel)
                ActiveScreen.SyncConflictCenter -> SyncConflictCenterScreen(viewModel)
                else -> {}
            }
        }

        // Role-Specific Tabbed Footer Navigation
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            when (currentRole) {
                "Admin" -> {
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.AdminStudentsList,
                        onClick = { viewModel.setScreen(ActiveScreen.AdminStudentsList) },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Students") },
                        label = { Text("Students") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.AdminAdmissions,
                        onClick = { viewModel.setScreen(ActiveScreen.AdminAdmissions) },
                        icon = { Icon(Icons.Default.AddBox, contentDescription = "Enroll") },
                        label = { Text("Admissions") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.AdminSchedules,
                        onClick = { viewModel.setScreen(ActiveScreen.AdminSchedules) },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedules") },
                        label = { Text("Staffing") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                }
                "Teacher" -> {
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.TeacherAttendance,
                        onClick = { viewModel.setScreen(ActiveScreen.TeacherAttendance) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Attendance") },
                        label = { Text("Attendance") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.TeacherGrades,
                        onClick = { viewModel.setScreen(ActiveScreen.TeacherGrades) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Grading") },
                        label = { Text("Grades") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                }
                "Student" -> {
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.StudentPortal,
                        onClick = { viewModel.setScreen(ActiveScreen.StudentPortal) },
                        icon = { Icon(Icons.Default.School, contentDescription = "Courses") },
                        label = { Text("My Courses") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                }
                "Parent" -> {
                    NavigationBarItem(
                        selected = activeScreen == ActiveScreen.ParentPortal,
                        onClick = { viewModel.setScreen(ActiveScreen.ParentPortal) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Progress") },
                        label = { Text("Activity") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                }
            }

            // Universal Messaging Portal Option
            NavigationBarItem(
                selected = activeScreen == ActiveScreen.MessagingPortal,
                onClick = { viewModel.setScreen(ActiveScreen.MessagingPortal) },
                icon = { Icon(Icons.Default.Chat, contentDescription = "Global portal") },
                label = { Text("Portal Msg") },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            )
        }
    }
}

// ================= ADMIN SUB-VIEWS =================

@Composable
fun AdminAdmissionsScreen(viewModel: InstituteViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var selectedCourseId by remember { mutableStateOf("ADIT") }
    var admissionFeePaid by remember { mutableStateOf("1500") }
    
    // Upload document simulations
    var uploadedDocName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val courses by viewModel.courses.collectAsState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Student Registration Form",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Admit a new custom student and configure documentation parameters.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Contact Email ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Contact Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("Parent / Guardian Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("Parent Active Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    // Course selection dropdown custom layout
                    Column {
                        Text("Course Program Selection:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            courses.forEach { c ->
                                FilterChip(
                                    selected = selectedCourseId == c.id,
                                    onClick = { selectedCourseId = c.id },
                                    label = { Text(c.id, fontSize = 11.sp) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = admissionFeePaid,
                        onValueChange = { admissionFeePaid = it },
                        label = { Text("Initial Admission Fee Form Payment (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    // Document Upload Module
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch {
                                    isUploading = true
                                    delay(1000)
                                    uploadedDocName = "Student_ID_and_HighSchool_Certificate.pdf"
                                    isUploading = false
                                }
                            }
                            .padding(16.dp),
                        color = Color.Transparent
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Upload doc",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isUploading) "Simulating Secure Upload..." 
                                       else uploadedDocName ?: "Simulate Registration forms / Doc Uploads (Click)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uploadedDocName != null) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                            )
                            if (uploadedDocName == null) {
                                Text("Attach Identity Marksheets & PDF enrolls", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && email.isNotBlank()) {
                                viewModel.addStudent(
                                    name, email, phone, parentName, parentPhone,
                                    selectedCourseId, uploadedDocName, admissionFeePaid.toDoubleOrNull() ?: 0.0
                                )
                                // Clear input form fields
                                name = ""
                                email = ""
                                phone = ""
                                parentName = ""
                                parentPhone = ""
                                uploadedDocName = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                    ) {
                        Text("Verify & Submit Admissions Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStudentsScreen(viewModel: InstituteViewModel) {
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val billing by viewModel.billing.collectAsState()
    var selectedCourseFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = students.filter { s ->
        (selectedCourseFilter == null || s.courseId == selectedCourseFilter) &&
        (s.name.contains(searchQuery, ignoreCase = true) || s.phone.contains(searchQuery))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Active Registered Students",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Track student profiles, balance warning indexes, and cloud status.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filters UI
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name/phone") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCourseFilter == null,
                onClick = { selectedCourseFilter = null },
                label = { Text("ALL") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
            courses.forEach { c ->
                FilterChip(
                    selected = selectedCourseFilter == c.id,
                    onClick = { selectedCourseFilter = c.id },
                    label = { Text(c.id) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Student lists
        if (filteredStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No active student records matched criteria.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStudents) { student ->
                    val studBilling = billing.filter { it.studentId == student.id }
                    val hasPendingTuition = studBilling.any { it.status == "Pending" || it.status == "Overdue" }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = student.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Cloud status indicator
                                Badge(
                                    containerColor = when (student.cloudSyncState) {
                                        "Synced" -> Color(0xFF10B981)
                                        "OutOfSync" -> Color(0xFFF59E0B)
                                        else -> Color(0xFFB3261E) // Conflict State
                                    },
                                    contentColor = Color.White
                                ) {
                                    Text(student.cloudSyncState)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Class: ${student.courseId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Text("● Reg Date: ${student.enrollmentDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                              ) {
                                Column {
                                    Text("Contact: ${student.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("Father/Mother: ${student.parentName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Tuition Pending indicator
                                    if (hasPendingTuition) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Fee Pending",
                                            tint = Color(0xFF6750A4),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Uploaded Doc check
                                    Icon(
                                        imageVector = if (student.uploadedDocName != null) Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = "Document Upload Checked",
                                        tint = if (student.uploadedDocName != null) Color(0xFF10B981) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSchedulesScreen(viewModel: InstituteViewModel) {
    val schedules by viewModel.schedules.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Teacher Slots & Classroom Schedules",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Faculty coordinate matrix mapped to computer lab allocations.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(schedules) { sch ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sch.teacherName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(sch.labNumber, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Course: " + sch.courseId,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⏰ ${sch.batchTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text("🗓️ ${sch.daysOfWeek}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}


// ================= TEACHER SUB-VIEWS =================

@Composable
fun TeacherAttendanceScreen(viewModel: InstituteViewModel) {
    val students by viewModel.students.collectAsState()
    val scope = rememberCoroutineScope()

    var activeTrackingMethod by remember { mutableStateOf("Auto-RFID Target Mode") }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "RFID / GPS Daily Attendance",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Mark manual entries or trigger simulated computer automated RFID tracking.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Simulated Automatic tracker toggles
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Automation Engine Settings:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeTrackingMethod == "Auto-RFID Target Mode",
                        onClick = { activeTrackingMethod = "Auto-RFID Target Mode" },
                        label = { Text("Auto RFID Tracker") }
                    )
                    FilterChip(
                        selected = activeTrackingMethod == "GPS Classroom Bounds",
                        onClick = { activeTrackingMethod = "GPS Classroom Bounds" },
                        label = { Text("GPS Area Verify") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        scope.launch {
                            notificationMessage = "Scanning institute receivers..."
                            delay(1200)
                            // Auto check students
                            students.forEach { s ->
                                viewModel.submitAttendance(s.id, isPresent = true, method = activeTrackingMethod)
                            }
                            notificationMessage = "Scan Success! 100% of present active rosters logged in room database."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Mock Auto-Scan RFID", fontWeight = FontWeight.SemiBold)
                }

                notificationMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(top = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(students) { student ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(student.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Course ID: ${student.courseId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { viewModel.submitAttendance(student.id, true, "Teacher Panel") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                            ) {
                                Text("Present", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.submitAttendance(student.id, false, "Teacher Panel") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E), contentColor = Color.White)
                            ) {
                                Text("Absent", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherGradesScreen(viewModel: InstituteViewModel) {
    val students by viewModel.students.collectAsState()
    var selectedStudentId by remember { mutableStateOf<Int?>(null) }
    var examTitle by remember { mutableStateOf("") }
    var examMarksObtained by remember { mutableStateOf("") }
    var evaluationRemarks by remember { mutableStateOf("") }
    var entryMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Exam Marking & Student Grading",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Update grade records. Submitted marks instantly trigger parent notifications.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                
                // Select student dropdown mock view
                Column {
                    Text("Select Learner Profile:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        students.forEach { s ->
                            FilterChip(
                                selected = selectedStudentId == s.id,
                                onClick = { selectedStudentId = s.id },
                                label = { Text(s.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Exam Name (e.g., Midterm Theory, Final Lab)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )

                OutlinedTextField(
                    value = examMarksObtained,
                    onValueChange = { examMarksObtained = it },
                    label = { Text("Marks Obtained (Max 100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )

                OutlinedTextField(
                    value = evaluationRemarks,
                    onValueChange = { evaluationRemarks = it },
                    label = { Text("Teacher feedback notes for parents") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )

                Button(
                    onClick = {
                        val activeId = selectedStudentId
                        val marks = examMarksObtained.toIntOrNull()
                        if (activeId != null && marks != null && examTitle.isNotBlank()) {
                            viewModel.submitMarks(activeId, examTitle, marks, evaluationRemarks)
                            entryMessage = "Grade successfully logged and parent SMS schedule added!"
                            examTitle = ""
                            examMarksObtained = ""
                            evaluationRemarks = ""
                        } else {
                            entryMessage = "Error: Ensure student, exam title and scores are valid."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                ) {
                    Text("Register Official Grades", fontWeight = FontWeight.Bold)
                }

                entryMessage?.let { msg ->
                    Text(msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}


// ================= STUDENT & PARENT BI-DASHBOARDS =================

@Composable
fun StudentDashboardScreen(viewModel: InstituteViewModel) {
    val students by viewModel.students.collectAsState()
    val selectedId by viewModel.selectedStudentIdForPortal.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val billing by viewModel.billing.collectAsState()
    val attendance by viewModel.attendance.collectAsState()

    val currentStudent = students.find { it.id == selectedId }
    val studGrades = grades.filter { it.studentId == selectedId }
    val studBilling = billing.filter { it.studentId == selectedId }
    val studAttendance = attendance.filter { it.studentId == selectedId }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Student Self-Service Portal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("View current course syllabus, pending tuition, and class progress maps.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }

        // Student Selector to view different student portal details
        item {
            Column {
                Text("Logged in as Profile:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    students.forEach { s ->
                        FilterChip(
                            selected = s.id == selectedId,
                            onClick = { viewModel.setSelectedStudentForPortal(s.id) },
                            label = { Text(s.name) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        if (currentStudent == null) {
            item {
                Text("Select student profile to load portal.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        } else {
            // Profile Card Info
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currentStudent.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Text(currentStudent.courseId, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reg Email: ${currentStudent.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("Phone: ${currentStudent.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            // Tuition Fee Alerts & Settlements
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Payment Schedules & Invoices", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (studBilling.isEmpty()) {
                            Text("All clear! No current pending invoices found.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                        } else {
                            studBilling.forEach { invoice ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(invoice.remark, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Due date: ${invoice.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(
                                            containerColor = if (invoice.status == "Paid") Color(0xFF10B981) else Color(0xFFB3261E),
                                            contentColor = Color.White
                                        ) {
                                            Text(invoice.status, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (invoice.status != "Paid") {
                                            Button(
                                                onClick = { viewModel.collectTuitionFee(invoice, payInFull = true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Pay Fees", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Attendance Progress Circular dial and checklist
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("My Personal Attendance Log", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(70.dp).padding(6.dp)
                            ) {
                                val progressTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = progressTrackColor,
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                    drawArc(
                                        color = Color(0xFF10B981),
                                        startAngle = -90f,
                                        sweepAngle = 300f, // Simulated 85% Attendance mark
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                }
                                Text("85%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Automated Smart GPS Checked", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text("No absent blocks logged for previous 7 active days. Exceptional standard maintained.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            // Academic Grade cards
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Exam Reports", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (studGrades.isEmpty()) {
                            Text("No examinations currently logged yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        } else {
                            studGrades.forEach { grade ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(grade.examName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Remarks: ${grade.remarks}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    Text("${grade.marksObtained}/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentDashboardScreen(viewModel: InstituteViewModel) {
    val students by viewModel.students.collectAsState()
    val selectedId by viewModel.selectedStudentIdForPortal.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val billing by viewModel.billing.collectAsState()
    val attendance by viewModel.attendance.collectAsState()

    val currentStudent = students.find { it.id == selectedId }
    val studGrades = grades.filter { it.studentId == selectedId }
    val studBilling = billing.filter { it.studentId == selectedId }
    val studAttendance = attendance.filter { it.studentId == selectedId }

    var showReportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Parent Guardian Control Desk", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Track your ward's real-time academic index with complete transparency.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }

        // Student Selector to view different student portal details
        item {
            Column {
                Text("Select Ward's File:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    students.forEach { s ->
                        FilterChip(
                            selected = s.id == selectedId,
                            onClick = { viewModel.setSelectedStudentForPortal(s.id) },
                            label = { Text(s.name) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        if (currentStudent == null) {
            item {
                Text("Select parent ward to load records.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        } else {
            // General status indicators
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ward Profile status:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Badge(containerColor = Color(0xFF10B981), contentColor = Color.White) { Text("Active Roster", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentStudent.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Enrolled in Technology Course Program: ${currentStudent.courseId}", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Real-time Pending Tuition Warning Card
            item {
                val tuitionPendingRecord = studBilling.filter { it.status == "Pending" || it.status == "Overdue" }
                val hasPending = tuitionPendingRecord.isNotEmpty()
                val targetContainerColor = if (hasPending) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                val targetBorderColor = if (hasPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                val targetHeaderColor = if (hasPending) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary

                Card(
                    colors = CardDefaults.cardColors(containerColor = targetContainerColor),
                    border = BorderStroke(1.dp, targetBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Fee Overdue",
                                tint = if (hasPending) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pending Tuition & Balance Status",
                                fontWeight = FontWeight.Bold,
                                color = targetHeaderColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (tuitionPendingRecord.isEmpty()) {
                            Text("Wonderful! Registration and tuition fees paid in full with zero balance dues.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        } else {
                            tuitionPendingRecord.forEach { item ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.remark, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text("INR ${item.amountDue}", fontSize = 13.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Overdue target date: ${item.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        Button(
                                            onClick = { viewModel.collectTuitionFee(item, payInFull = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Clear Tuition Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Progress Gauge
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Course Learning Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Class Attendance index:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text("Present: 85% sessions count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("Grading Marks status:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                val avgGrade = if (studGrades.isNotEmpty()) studGrades.map { it.marksObtained }.average().toInt() else "Exempt"
                                Text("Result Average: $avgGrade%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showReportDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Assessment, contentDescription = "Report")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Instant Official Performance Report", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Official Mock PDF Performance letter Dialog
    if (showReportDialog && currentStudent != null) {
        val calculatedAverage = if (studGrades.isNotEmpty()) studGrades.map { it.marksObtained }.average().toInt().toString() + "%" else "N/A"
        Dialog(onDismissRequest = { showReportDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "IT COMPUTER STUDIES (ITCS)",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Lucknow Branch • Certified Academic Assessment",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("STUDENT NAME: ${currentStudent.name}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text("COURSE ENROLLED: ${currentStudent.courseId}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    Text("ACADEMIC ATTENDANCE: 85% checked", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    Text("TOTAL GRADES LOGGED IN DATABASE: ${studGrades.size}", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    Text("PERFORMANCE AVERAGE EVALUATION: $calculatedAverage", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Official Coordinator General Comment:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    Text(
                        text = "The learner has shown dedicated conceptual progress in project development, exhibiting high proficiency in computer diagnostics. No outstanding balance reports are due.",
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Authorized Digital Seal Verified", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Button(
                            onClick = { showReportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                        ) {
                            Text("Print / Close Document")
                        }
                    }
                }
            }
        }
    }
}


// ================= PORTAL CHAT SYSTEM SUB-VIEW =================

@Composable
fun MessagingPortalScreen(viewModel: InstituteViewModel) {
    val messages by viewModel.messages.collectAsState()
    var userMessageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Institutional Portal Messaging",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Instant messaging channel for staff, parents, and registered learners.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Message Thread Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (messages.isEmpty()) {
                Text("No messages logged in the database yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isSystemSender = msg.senderRole == "Admin" || msg.senderRole == "Teacher"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalAlignment = if (isSystemSender) Alignment.Start else Alignment.End
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSystemSender) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg.senderRole,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = if (isSystemSender) 0.dp else 8.dp,
                                            topEnd = if (isSystemSender) 8.dp else 0.dp,
                                            bottomStart = 8.dp,
                                            bottomEnd = 8.dp
                                        )
                                    )
                                    .background(if (isSystemSender) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    color = if (isSystemSender) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userMessageText,
                onValueChange = { userMessageText = it },
                placeholder = { Text("Write chat message...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userMessageText.isNotBlank()) {
                        viewModel.postMessage(userMessageText)
                        userMessageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}


// ================= DYNAMIC OFFLINE SYNC AND CONFLICT CENTER SUB-VIEW =================

@Composable
fun SyncConflictCenterScreen(viewModel: InstituteViewModel) {
    val conflicts by viewModel.conflicts.collectAsState()
    val pendingSyncs by viewModel.pendingSyncs.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Offline Sync & Conflict Solver",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Review and resolve sync conflicts when compiling offline cached documents with the server.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // General status indicator card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Connection Index Status:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOnline) "🟢 Online Sync System Ready" else "🔴 Offline Cache State (Mock Offline)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { viewModel.triggerManualSync() },
                        enabled = isOnline,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                    ) {
                        Text("Sync Now Cache")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${pendingSyncs.size} pending modifications waiting offline in standard SQL cache schema.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        if (conflicts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Synced",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No sync conflicts recorded inside the system database.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    Text("All offline cache packets match primary cloud models.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("⚠️ ATTENTION: Core database conflict found!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    Text("Conflict occurred due to duplicate modifications in offline state and current server. Resolve below manually:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }

                items(conflicts) { conf ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Student: " + conf.studentName,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)) {
                                    Text("LOCAL FILE (Offline edit)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Email: ${conf.localValue.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("Status: ${conf.localValue.registrationStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.resolveConflict(conf, chooseLocal = true) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("Keep Local edit", fontSize = 10.sp, color = Color.White)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)) {
                                    Text("SERVER FILE (Cloud duplicate)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Email: ${conf.cloudValue.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("Status: ${conf.cloudValue.registrationStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.resolveConflict(conf, chooseLocal = false) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("Use Server copy", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
