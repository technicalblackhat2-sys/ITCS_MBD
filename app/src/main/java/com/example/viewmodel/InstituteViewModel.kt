package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ActiveScreen {
    BiometricLock,
    RoleSelector,
    AdminAdmissions,
    AdminStudentsList,
    AdminSchedules,
    TeacherAttendance,
    TeacherGrades,
    StudentPortal,
    ParentPortal,
    MessagingPortal,
    SyncConflictCenter
}

data class InAppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType, // Tuition, Announcement, Deadline
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

enum class NotificationType {
    Tuition, Announcement, Deadline
}

class InstituteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InstituteRepository

    // Database Flows
    val courses: StateFlow<List<Course>>
    val students: StateFlow<List<Student>>
    val attendance: StateFlow<List<Attendance>>
    val grades: StateFlow<List<Grade>>
    val billing: StateFlow<List<Billing>>
    val schedules: StateFlow<List<TeacherSchedule>>
    val messages: StateFlow<List<PortalMessage>>
    val pendingSyncs: StateFlow<List<PendingSync>>
    val conflicts: StateFlow<List<SyncConflict>>
    val isOnline: StateFlow<Boolean>

    // Role-based Navigation States
    private val _currentRole = MutableStateFlow("Admin") // Admin, Teacher, Student, Parent
    val currentRole = _currentRole.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.RoleSelector)
    val activeScreen = _activeScreen.asStateFlow()

    // Biometrics Account Security
    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    private val _isUserAuthenticated = MutableStateFlow(true)
    val isUserAuthenticated = _isUserAuthenticated.asStateFlow()

    // Active User Context (For Student/Parent portals)
    private val _selectedStudentIdForPortal = MutableStateFlow<Int?>(null)
    val selectedStudentIdForPortal = _selectedStudentIdForPortal.asStateFlow()

    // Real-Time Notification Streams
    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Quick filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = InstituteRepository(database.dao())

        courses = repository.courses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        students = repository.students.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        attendance = repository.attendance.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        grades = repository.grades.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        billing = repository.billing.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        schedules = repository.schedules.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        messages = repository.messages.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        pendingSyncs = repository.pendingSyncs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        conflicts = repository.conflicts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        isOnline = repository.isOnline.stateIn(viewModelScope, SharingStarted.Lazily, true)

        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            generateRealTimeNotifications()
        }
    }

    // Toggle simulated network status
    fun toggleInternetConnectivity() {
        val nextStatus = !isOnline.value
        repository.setOnlineStatus(nextStatus)
        if (nextStatus) {
            // Re-sync immediately once connectivity is restored!
            triggerManualSync()
        }
    }

    // Set Role and Route correctly
    fun selectRole(role: String) {
        _currentRole.value = role
        if (_biometricEnabled.value) {
            _isUserAuthenticated.value = false
            _activeScreen.value = ActiveScreen.BiometricLock
        } else {
            routeToRoleDefaults(role)
        }
    }

    private fun routeToRoleDefaults(role: String) {
        _isUserAuthenticated.value = true
        when (role) {
            "Admin" -> _activeScreen.value = ActiveScreen.AdminStudentsList
            "Teacher" -> _activeScreen.value = ActiveScreen.TeacherAttendance
            "Student" -> {
                viewModelScope.launch {
                    val list = students.value
                    if (list.isNotEmpty()) {
                        _selectedStudentIdForPortal.value = list.first().id
                    }
                    _activeScreen.value = ActiveScreen.StudentPortal
                }
            }
            "Parent" -> {
                viewModelScope.launch {
                    val list = students.value
                    if (list.isNotEmpty()) {
                        _selectedStudentIdForPortal.value = list.first().id
                    }
                    _activeScreen.value = ActiveScreen.ParentPortal
                }
            }
        }
    }

    // Handle Biometric authenticated mock check
    fun authenticateWithBiometrics(success: Boolean) {
        if (success) {
            _isUserAuthenticated.value = true
            routeToRoleDefaults(_currentRole.value)
        }
    }

    fun toggleBiometricSetup() {
        _biometricEnabled.value = !_biometricEnabled.value
    }

    fun setSelectedStudentForPortal(studentId: Int) {
        _selectedStudentIdForPortal.value = studentId
    }

    fun setScreen(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Insert Student Profile
    fun addStudent(name: String, email: String, phone: String, parent: String, parentPhone: String, courseId: String, docName: String?, initialFeePaid: Double) {
        viewModelScope.launch {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val studentObject = Student(
                name = name,
                email = email,
                phone = phone,
                parentName = parent,
                parentPhone = parentPhone,
                courseId = courseId,
                enrollmentDate = format.format(Date()),
                registrationStatus = if (docName != null) "Approved" else "Pending"
            )
            repository.registerNewStudent(studentObject, docName, initialFeePaid)
            generateRealTimeNotifications()
        }
    }

    // Dynamic Attendance Mark
    fun submitAttendance(studentId: Int, isPresent: Boolean, method: String) {
        viewModelScope.launch {
            val status = if (isPresent) "Present" else "Absent"
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val record = Attendance(
                studentId = studentId,
                date = format.format(Date()),
                status = status,
                verifiedBy = method
            )
            repository.recordAttendance(record)
        }
    }

    // Submit Marks for Grading Module
    fun submitMarks(studentId: Int, examName: String, marks: Int, remarks: String) {
        viewModelScope.launch {
            val currentStudent = students.value.find { it.id == studentId } ?: return@launch
            val record = Grade(
                studentId = studentId,
                courseId = currentStudent.courseId,
                examName = examName,
                marksObtained = marks,
                remarks = remarks,
                dateGraded = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.addGrade(record)
        }
    }

    // Collect Tuition Fee Payment
    fun collectTuitionFee(billingRecord: Billing, payInFull: Boolean) {
        viewModelScope.launch {
            val paidAmount = if (payInFull) billingRecord.amountDue else billingRecord.amountDue / 2
            val updated = billingRecord.copy(
                amountPaid = billingRecord.amountPaid + paidAmount,
                status = if (payInFull) "Paid" else "Paid Partial"
            )
            repository.recordPayment(updated)
            generateRealTimeNotifications()
        }
    }

    // Send Message Portal Interaction
    fun postMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val record = PortalMessage(
                senderName = when (_currentRole.value) {
                    "Admin" -> "Administrator"
                    "Teacher" -> "Instructor Panel"
                    "Student" -> students.value.find { it.id == _selectedStudentIdForPortal.value }?.name ?: "Student"
                    "Parent" -> "Parent of " + (students.value.find { it.id == _selectedStudentIdForPortal.value }?.name ?: "Learner")
                    else -> "Anonymous"
                },
                senderRole = _currentRole.value,
                content = content
            )
            repository.sendMessage(record)
        }
    }

    // Sync Commands & Conflict management
    fun triggerManualSync() {
        viewModelScope.launch {
            repository.testOrPerformSync()
        }
    }

    fun resolveConflict(conflict: SyncConflict, chooseLocal: Boolean) {
        viewModelScope.launch {
            repository.resolveConflict(conflict, chooseLocal)
        }
    }

    fun clearConflicts() {
        repository.clearConflictsExplicitly()
    }

    // Dynamic warning alert generators for Tuition & Deadlines
    private fun generateRealTimeNotifications() {
        viewModelScope.launch {
            val list = mutableListOf<InAppNotification>()

            // 1. Audit pending tuition fees
            val bills = billing.value
            val pendingCount = bills.count { it.status == "Pending" }
            if (pendingCount > 0) {
                list.add(InAppNotification(
                    title = "Pending Tuition Real-Time Warning",
                    message = "There are $pendingCount outstanding fee installments requiring collection. Auto-reminders sent to parent registers.",
                    type = NotificationType.Tuition
                ))
            }

            // 2. Announcements
            list.add(InAppNotification(
                title = "National Cyber Security Championship",
                message = "The Ethical Hacking Lab will host the zonal penetration trials on Tuesday 10:00 AM.",
                type = NotificationType.Announcement
            ))

            // 3. Document submissions
            val pendingRegs = students.value.count { it.registrationStatus == "Pending" || it.uploadedDocName.isNullOrEmpty() }
            if (pendingRegs > 0) {
                list.add(InAppNotification(
                    title = "Missing Document Deadline Reminder",
                    message = "$pendingRegs newly registered students require verification forms and proof of enrollment uploads.",
                    type = NotificationType.Deadline
                ))
            }

            _notifications.value = list
        }
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }
}
