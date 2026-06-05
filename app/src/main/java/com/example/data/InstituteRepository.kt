package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

data class SyncConflict(
    val studentId: Int,
    val studentName: String,
    val localValue: Student,
    val cloudValue: Student
)

class InstituteRepository(private val dao: InstituteDao) {

    // Expose all data streams
    val courses: Flow<List<Course>> = dao.getAllCoursesFlow()
    val students: Flow<List<Student>> = dao.getAllStudentsFlow()
    val attendance: Flow<List<Attendance>> = dao.getAllAttendanceFlow()
    val grades: Flow<List<Grade>> = dao.getAllGradesFlow()
    val billing: Flow<List<Billing>> = dao.getAllBillingFlow()
    val schedules: Flow<List<TeacherSchedule>> = dao.getAllSchedulesFlow()
    val messages: Flow<List<PortalMessage>> = dao.getAllMessagesFlow()
    val pendingSyncs: Flow<List<PendingSync>> = dao.getAllPendingSyncsFlow()

    // Conflict Monitoring State
    private val _conflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val conflicts = _conflicts.asStateFlow()

    // Simulator Connectivity
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value = online
    }

    // Seed Data if Database is Empty
    suspend fun seedDatabaseIfEmpty() {
        val existingCourses = courses.first()
        if (existingCourses.isEmpty()) {
            // Seeding courses based on ITCS URL info (CCC, ADIT, Stock Market, Ethical Hacking, AutoCAD, CAA)
            val initialCourses = listOf(
                Course("ADIT", "Advance Diploma in Information Technology", 15, 15000.0, "Fundamental, Windows, MS Office, HTML, CSS, JavaScript, Python, Database Systems, Network Fundamentals, Project Development"),
                Course("CCC", "Course on Computer Concepts", 3, 3000.0, "Computer Introduction, GUI Operating Systems, Word Processing, Spreadsheet applications, Presentation guides, WWW and Web Browsers, E-mail & Social Networking, Digital Financial Services"),
                Course("CAA", "Certificate in Advance Accounting", 6, 8000.0, "Accounting Principles, Manual Bookkeeping, Tally ERP 9, GST Taxation, Income Tax Basics, Excel Auditing, Tally Prime Operations"),
                Course("AUTOCAD", "Computer Aided Design (AutoCAD)", 3, 6000.0, "2D Drafting, Isometric Drawings, 3D Modeling, Architectural Layouts, Isometric Projections, Section Plan Drawings"),
                Course("STOCK", "Stock Market & Technical Analysis", 2, 5000.0, "Fundamental Market Analysis, Technical Chart Patterns, Candlestick Indicators, Risk Management, Futures & Options, Portfolio Diversification"),
                Course("ETHICAL", "Ethical Hacking & Cyber Security", 4, 12000.0, "Penetration Testing, Network Sniffing, Cryptography, System Hacking, Wi-Fi Analysis, Web Application Exploit testing, Defensive Firewalls")
            )
            for (c in initialCourses) {
                dao.insertCourse(c)
            }

            // Seed Schedules (Teacher scheduling - using real names scraped/found on the website or appropriate heads)
            val initialSchedules = listOf(
                TeacherSchedule(teacherName = "Mr. Ratnesh Dubey", courseId = "ADIT", batchTime = "09:00 AM - 11:00 AM", daysOfWeek = "Mon, Wed, Fri", labNumber = "Lab A"),
                TeacherSchedule(teacherName = "Mr. Shariq Mirza", courseId = "STOCK", batchTime = "11:00 AM - 12:30 PM", daysOfWeek = "Tue, Thu, Sat", labNumber = "Seminar Room"),
                TeacherSchedule(teacherName = "Mr. Aftab Suhail", courseId = "CCC", batchTime = "01:00 PM - 02:30 PM", daysOfWeek = "Mon, Wed, Fri", labNumber = "Lab B"),
                TeacherSchedule(teacherName = "Mr. Prateek Thakur", courseId = "CAA", batchTime = "03:00 PM - 05:00 PM", daysOfWeek = "Mon, Tue, Thu", labNumber = "Finance Lab")
            )
            for (s in initialSchedules) {
                dao.insertSchedule(s)
            }

            // Seed Some Students (Role mock database representation)
            val initialStudents = listOf(
                Student(name = "Aman Verma", email = "aman@gmail.com", phone = "9876543210", parentName = "S. Verma", parentPhone = "9876543211", courseId = "ADIT", enrollmentDate = "2026-05-10", registrationStatus = "Approved", biometricsEnrolled = true, cloudSyncState = "Synced"),
                Student(name = "Riya Dwivedi", email = "riya@gmail.com", phone = "8876543212", parentName = "M. Dwivedi", parentPhone = "8876543213", courseId = "CCC", enrollmentDate = "2026-06-01", registrationStatus = "Pending", biometricsEnrolled = false, cloudSyncState = "Synced"),
                Student(name = "Utkarsh Singh", email = "utkarsh@gmail.com", phone = "7776543214", parentName = "K. Singh", parentPhone = "7776543215", courseId = "ETHICAL", enrollmentDate = "2026-04-15", registrationStatus = "Approved", biometricsEnrolled = true, cloudSyncState = "Synced")
            )
            for (st in initialStudents) {
                val nid = dao.insertStudent(st).toInt()
                // Auto seed some fee invoices
                dao.insertBilling(Billing(studentId = nid, studentName = st.name, amountDue = 1500.0, amountPaid = 0.0, dueDate = "2026-06-15", status = "Pending", remark = "Installment 1"))
                dao.insertBilling(Billing(studentId = nid, studentName = st.name, amountDue = 1500.0, amountPaid = 1500.0, dueDate = "2026-05-15", status = "Paid", remark = "Admission Fee"))
                
                // Attendance
                dao.insertAttendance(Attendance(studentId = nid, date = "2026-06-03", status = "Present", verifiedBy = "Auto-GPS"))
                dao.insertAttendance(Attendance(studentId = nid, date = "2026-06-04", status = "Present", verifiedBy = "Instructor"))
                dao.insertAttendance(Attendance(studentId = nid, date = "2026-06-05", status = "Late", verifiedBy = "Auto-GPS"))

                // Grades
                dao.insertGrade(Grade(studentId = nid, courseId = st.courseId, examName = "Unit Test 1", marksObtained = 85, remarks = "Excellent Programming logic", dateGraded = "2026-05-25"))
            }

            // Messages
            val initialMessages = listOf(
                PortalMessage(senderName = "Mr. Ratnesh Dubey", senderRole = "Admin", content = "Welcome to ITCS Connect. Registrations for the Web Security & Professional Technical Analysis certification are now open!"),
                PortalMessage(senderName = "Mr. Aftab Suhail", senderRole = "Teacher", content = "CCC Practical exam scheduling is completed. Please check your personalized panel."),
                PortalMessage(senderName = "Aman Verma", senderRole = "Student", content = "Sir, I have updated my ADIT mid-term project in the portal.")
            )
            for (m in initialMessages) {
                dao.insertMessage(m)
            }
        }
    }

    // Dynamic operations with offline tracking
    suspend fun registerNewStudent(student: Student, docName: String?, initialFee: Double) {
        val newSt = student.copy(
            uploadedDocName = docName,
            cloudSyncState = if (_isOnline.value) "Synced" else "OutOfSync"
        )
        val newId = dao.insertStudent(newSt).toInt()

        // Generate Registration Billing Invoice
        dao.insertBilling(Billing(
            studentId = newId,
            studentName = student.name,
            amountDue = initialFee,
            amountPaid = if (_isOnline.value) initialFee else 0.0,
            dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            status = if (_isOnline.value) "Paid" else "Pending",
            remark = "Admission Form Fee"
        ))

        if (!_isOnline.value) {
            dao.insertPendingSync(PendingSync(
                tableName = "students",
                recordId = newId,
                actionType = "INSERT",
                changeSummary = "Registered New Student: ${student.name}"
            ))
        }
    }

    suspend fun recordAttendance(attendance: Attendance) {
        dao.insertAttendance(attendance)
        if (!_isOnline.value) {
            dao.insertPendingSync(PendingSync(
                tableName = "attendance",
                recordId = 0, // General tracking
                actionType = "INSERT",
                changeSummary = "Recorded Attendance of Student ID:${attendance.studentId} as ${attendance.status}"
            ))
        }
    }

    suspend fun addGrade(grade: Grade) {
        dao.insertGrade(grade)
        if (!_isOnline.value) {
            dao.insertPendingSync(PendingSync(
                tableName = "grades",
                recordId = 0,
                actionType = "INSERT",
                changeSummary = "Added grade of ${grade.marksObtained}/${grade.totalMarks} for Student ID:${grade.studentId}"
            ))
        }
    }

    suspend fun recordPayment(billing: Billing) {
        dao.insertBilling(billing)
        if (!_isOnline.value) {
            dao.insertPendingSync(PendingSync(
                tableName = "billing",
                recordId = billing.id,
                actionType = "UPDATE",
                changeSummary = "Recorded Payment of INR ${billing.amountPaid} for ${billing.studentName}"
            ))
        }
    }

    suspend fun sendMessage(msg: PortalMessage) {
        dao.insertMessage(msg)
    }

    // Trigger Offline Sync and Handle Conflict Resolution
    suspend fun testOrPerformSync() {
        if (!_isOnline.value) return // Can't sync if offline

        val pending = dao.getAllPendingSyncsFlow().first()
        if (pending.isEmpty() && _conflicts.value.isEmpty()) return

        // Fetch students to simulate potential conflict checking
        val currentStudents = dao.getAllStudentsFlow().first()
        val detectedConflicts = mutableListOf<SyncConflict>()

        for (st in currentStudents) {
            if (st.cloudSyncState == "OutOfSync") {
                // Determine simulated cloud collision (specifically for demonstrating sync conflict handling!)
                // Let's assume there's an edit conflict in email or phone to demonstrate resolution
                val simulatedCloudStudent = st.copy(
                    email = if (st.email.contains("@")) {
                        st.email.substringBefore("@") + ".cloud@itcsedu.in"
                    } else {
                        "cloud@itcsedu.in"
                    },
                    phone = st.phone,
                    cloudSyncState = "Conflict"
                )
                
                // Flag a physical conflict in the flow
                detectedConflicts.add(SyncConflict(
                    studentId = st.id,
                    studentName = st.name,
                    localValue = st,
                    cloudValue = simulatedCloudStudent
                ))
                
                // Mark student in conflict state
                dao.updateStudent(st.copy(cloudSyncState = "Conflict"))
            }
        }

        if (detectedConflicts.isNotEmpty()) {
            _conflicts.value = detectedConflicts
        } else {
            // No conflicts detected, sync everything!
            for (st in currentStudents) {
                if (st.cloudSyncState == "OutOfSync") {
                    dao.updateStudent(st.copy(cloudSyncState = "Synced"))
                }
            }
            dao.clearAllPendingSyncs()
            _conflicts.value = emptyList()
        }
    }

    // Choose which option to resolve conflict with
    suspend fun resolveConflict(conflict: SyncConflict, chooseLocal: Boolean) {
        val resolvedStudent = if (chooseLocal) {
            conflict.localValue.copy(cloudSyncState = "Synced")
        } else {
            conflict.cloudValue.copy(cloudSyncState = "Synced")
        }
        dao.updateStudent(resolvedStudent)
        
        // Remove from list of active conflicts
        _conflicts.value = _conflicts.value.filter { it.studentId != conflict.studentId }
        
        // If conflicts are cleared, mark associated pending syncs resolved
        val remainingPending = dao.getAllPendingSyncsFlow().first()
        val matchingPending = remainingPending.firstOrNull { it.tableName == "students" && it.recordId == conflict.studentId }
        if (matchingPending != null) {
            dao.deletePendingSync(matchingPending.id)
        }
    }

    // Force clear conflict simulation
    fun clearConflictsExplicitly() {
        _conflicts.value = emptyList()
    }
}
