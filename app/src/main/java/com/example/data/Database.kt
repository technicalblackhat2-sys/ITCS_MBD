package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: String,
    val name: String,
    val durationMonths: Int,
    val totalFees: Double,
    val syllabus: String
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val parentName: String,
    val parentPhone: String,
    val courseId: String,
    val enrollmentDate: String,
    val registrationStatus: String = "Approved", // Pending, Approved
    val biometricsEnrolled: Boolean = false,
    val profilePicturePath: String? = null,
    val uploadedDocName: String? = null,
    val cloudSyncState: String = "Synced" // Synced, OutOfSync, Conflict
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val date: String, // YYYY-MM-DD
    val status: String, // Present, Absent, Late
    val verifiedBy: String // Auto-GPS, Instructor, Admin
)

@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val courseId: String,
    val examName: String,
    val marksObtained: Int,
    val totalMarks: Int = 100,
    val remarks: String,
    val dateGraded: String
)

@Entity(tableName = "billing")
data class Billing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val amountDue: Double,
    val amountPaid: Double,
    val dueDate: String,
    val status: String, // Paid, Pending, Overdue
    val remark: String = "Tuition Fee"
)

@Entity(tableName = "schedules")
data class TeacherSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherName: String,
    val courseId: String,
    val batchTime: String, // 09:00 AM - 11:00 AM
    val daysOfWeek: String, // Mon, Wed, Fri
    val labNumber: String
)

@Entity(tableName = "messages")
data class PortalMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderRole: String, // Admin, Teacher, Student, Parent
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_syncs")
data class PendingSync(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableName: String,
    val recordId: Int,
    val actionType: String, // INSERT, UPDATE, DELETE
    val changeSummary: String,
    val localTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface InstituteDao {

    // Courses
    @Query("SELECT * FROM courses")
    fun getAllCoursesFlow(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: String): Course?

    // Students
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: Int)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    // Grades
    @Query("SELECT * FROM grades ORDER BY dateGraded DESC")
    fun getAllGradesFlow(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId")
    fun getGradesForStudent(studentId: Int): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade)

    // Billing
    @Query("SELECT * FROM billing ORDER BY dueDate ASC")
    fun getAllBillingFlow(): Flow<List<Billing>>

    @Query("SELECT * FROM billing WHERE studentId = :studentId")
    fun getBillingForStudent(studentId: Int): Flow<List<Billing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBilling(billing: Billing)

    @Update
    suspend fun updateBilling(billing: Billing)

    // Schedules
    @Query("SELECT * FROM schedules")
    fun getAllSchedulesFlow(): Flow<List<TeacherSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: TeacherSchedule)

    // Messages
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<PortalMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: PortalMessage)

    // Pending Syncs
    @Query("SELECT * FROM pending_syncs")
    fun getAllPendingSyncsFlow(): Flow<List<PendingSync>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSync(sync: PendingSync)

    @Query("DELETE FROM pending_syncs WHERE id = :syncId")
    suspend fun deletePendingSync(syncId: Int)

    @Query("DELETE FROM pending_syncs")
    suspend fun clearAllPendingSyncs()
}

@Database(
    entities = [
        Course::class,
        Student::class,
        Attendance::class,
        Grade::class,
        Billing::class,
        TeacherSchedule::class,
        PortalMessage::class,
        PendingSync::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): InstituteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "institute_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
