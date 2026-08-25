package com.example.data

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FirebaseConnectionInfo(
    val isInitialized: Boolean,
    val projectId: String?,
    val applicationId: String?,
    val gcmSenderId: String?,
    val statusMessage: String
)

sealed class FirebaseTestResult {
    object Idle : FirebaseTestResult()
    object InProgress : FirebaseTestResult()
    data class Success(
        val docId: String,
        val timestamp: String,
        val serverResponseTimeMs: Long,
        val details: String
    ) : FirebaseTestResult()
    data class Error(val errorMessage: String, val helpTip: String) : FirebaseTestResult()
}

sealed class FirebaseSyncResult {
    object Idle : FirebaseSyncResult()
    object InProgress : FirebaseSyncResult()
    data class Success(val count: Int, val message: String) : FirebaseSyncResult()
    data class Error(val errorMessage: String) : FirebaseSyncResult()
}

class FirebaseSyncManager(private val context: Context) {

    private val tag = "FirebaseSyncManager"

    private fun ensureFirebaseInitialized(): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                true
            } else {
                val app = FirebaseApp.initializeApp(context)
                app != null
            }
        } catch (e: Exception) {
            Log.w(tag, "FirebaseApp.initializeApp failed: ${e.message}")
            false
        }
    }

    fun getConnectionInfo(): FirebaseConnectionInfo {
        val initialized = ensureFirebaseInitialized()
        return if (initialized) {
            try {
                val app = FirebaseApp.getInstance()
                val options = app.options
                FirebaseConnectionInfo(
                    isInitialized = true,
                    projectId = options.projectId,
                    applicationId = options.applicationId,
                    gcmSenderId = options.gcmSenderId,
                    statusMessage = "Firebase SDK is active and connected to project '${options.projectId ?: "default"}'"
                )
            } catch (e: Exception) {
                FirebaseConnectionInfo(
                    isInitialized = false,
                    projectId = null,
                    applicationId = null,
                    gcmSenderId = null,
                    statusMessage = "FirebaseApp is present but options could not be read."
                )
            }
        } else {
            FirebaseConnectionInfo(
                isInitialized = false,
                projectId = null,
                applicationId = null,
                gcmSenderId = null,
                statusMessage = "Firebase is not initialized. Please ensure google-services.json is in your app/ directory when building the APK."
            )
        }
    }

    suspend fun testConnection(): FirebaseTestResult {
        if (!ensureFirebaseInitialized()) {
            return FirebaseTestResult.Error(
                errorMessage = "Firebase is not initialized in this build.",
                helpTip = "Ensure 'google-services.json' is added to the 'app/' directory in your repository and rebuild the APK."
            )
        }

        return try {
            val startTime = System.currentTimeMillis()
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("test_connections").document("device_ping_${System.currentTimeMillis()}")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            val payload = hashMapOf(
                "timestamp" to dateStr,
                "timestampMillis" to System.currentTimeMillis(),
                "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
                "androidVersion" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                "appPackage" to context.packageName,
                "status" to "CONNECTED_OK",
                "pingMessage" to "MCQ Paper Generator app verified connection successfully!"
            )

            // Write to Firestore
            docRef.set(payload, SetOptions.merge()).await()

            // Read back to confirm round-trip communication
            val snapshot = docRef.get().await()
            val latency = System.currentTimeMillis() - startTime

            if (snapshot.exists()) {
                FirebaseTestResult.Success(
                    docId = docRef.id,
                    timestamp = dateStr,
                    serverResponseTimeMs = latency,
                    details = "Ping write & read verified in ${latency}ms! Check the 'test_connections' collection in your Firebase Console."
                )
            } else {
                FirebaseTestResult.Error(
                    errorMessage = "Document was written but could not be read back.",
                    helpTip = "Check your Firestore Security Rules in Firebase Console."
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Firebase connection test failed", e)
            val errorMsg = e.localizedMessage ?: e.message ?: "Unknown Firestore error"
            val helpTip = when {
                errorMsg.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Firestore Security Rules blocked write. Go to Firebase Console -> Firestore Database -> Rules, and set: 'allow read, write: if true;' for testing."
                errorMsg.contains("UNAVAILABLE", ignoreCase = true) || errorMsg.contains("network", ignoreCase = true) ->
                    "Network unreachable. Make sure your device has an active internet connection."
                errorMsg.contains("NOT_FOUND", ignoreCase = true) ->
                    "Firestore database not yet created. Go to Firebase Console -> Build -> Firestore Database -> Click 'Create database'."
                errorMsg.contains("Default FirebaseApp is not initialized", ignoreCase = true) ->
                    "Firebase configuration missing. Ensure 'google-services.json' is committed into 'app/' folder and rebuild APK."
                else ->
                    "Ensure Cloud Firestore is created in Firebase Console (Test mode) and google-services.json is valid."
            }
            FirebaseTestResult.Error(errorMessage = errorMsg, helpTip = helpTip)
        }
    }

    suspend fun backupQuestionsToCloud(questions: List<QuestionEntity>): FirebaseSyncResult {
        if (!ensureFirebaseInitialized()) {
            return FirebaseSyncResult.Error(
                "Firebase is not initialized. Please ensure google-services.json is in your app/ directory."
            )
        }
        if (questions.isEmpty()) {
            return FirebaseSyncResult.Error("No local questions to backup.")
        }
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val batch = firestore.batch()
            val collectionRef = firestore.collection("mcq_questions")

            questions.forEach { q ->
                val docRef = collectionRef.document("q_${q.id}")
                val map = hashMapOf(
                    "id" to q.id,
                    "subject" to q.subject,
                    "chapter" to q.chapter,
                    "topic" to q.topic,
                    "difficulty" to q.difficulty,
                    "tags" to q.tags,
                    "questionText" to q.questionText,
                    "optionA" to q.optionA,
                    "optionB" to q.optionB,
                    "optionC" to q.optionC,
                    "optionD" to q.optionD,
                    "correctOption" to q.correctOption,
                    "solutionText" to q.solutionText,
                    "marks" to q.marks,
                    "negativeMarks" to q.negativeMarks.toDouble(),
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            batch.commit().await()
            FirebaseSyncResult.Success(
                count = questions.size,
                message = "Successfully backed up ${questions.size} questions to Firestore collection 'mcq_questions'!"
            )
        } catch (e: Exception) {
            Log.e(tag, "Backup to Firestore failed", e)
            FirebaseSyncResult.Error(e.localizedMessage ?: "Failed to backup questions to Firestore.")
        }
    }
}
