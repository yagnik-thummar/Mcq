package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FirebaseConnectionInfo
import com.example.data.FirebaseSyncResult
import com.example.data.FirebaseTestResult
import com.example.ui.viewmodel.McqViewModel

@Composable
fun FirebaseConnectionDialog(
    viewModel: McqViewModel,
    onDismiss: () -> Unit
) {
    val connectionInfo by viewModel.firebaseConnectionInfo.collectAsStateWithLifecycle()
    val testState by viewModel.firebaseTestState.collectAsStateWithLifecycle()
    val syncState by viewModel.firebaseSyncState.collectAsStateWithLifecycle()
    val questionCount by viewModel.rawQuestions.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshFirebaseInfo()
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetFirebaseTestState()
            viewModel.resetFirebaseSyncState()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (connectionInfo.isInitialized)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (connectionInfo.isInitialized) Icons.Default.CloudDone else Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = if (connectionInfo.isInitialized)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Firebase Cloud Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (connectionInfo.isInitialized) "Connected & Initialized" else "Not Initialized",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (connectionInfo.isInitialized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connection Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoRow(
                            label = "Project ID",
                            value = connectionInfo.projectId ?: "None (Check google-services.json)"
                        )
                        InfoRow(
                            label = "Package Name",
                            value = "com.aistudio.mcqgenerator.kqrzp"
                        )
                        if (!connectionInfo.applicationId.isNullOrBlank()) {
                            InfoRow(
                                label = "App ID",
                                value = connectionInfo.applicationId ?: ""
                            )
                        }
                    }
                }

                // Live Test Connection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiTethering,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Live Ping Test",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (testState is FirebaseTestResult.InProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Text(
                            text = "Writes a live verification ping to Firestore collection 'test_connections' and measures server round-trip latency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Result feedback
                        when (val state = testState) {
                            is FirebaseTestResult.Success -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Verified! Latency: ${state.serverResponseTimeMs}ms",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1B5E20)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.details,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "Doc: ${state.docId}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF388E3C)
                                        )
                                    }
                                }
                            }
                            is FirebaseTestResult.Error -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Connection Notice",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.errorMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💡 Tip: ${state.helpTip}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }

                        Button(
                            onClick = { viewModel.testFirebaseCloudConnection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_ping_firebase"),
                            enabled = testState !is FirebaseTestResult.InProgress,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Ping to Firestore")
                        }
                    }
                }

                // Cloud Backup Action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "MCQ Bank Cloud Backup",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Sync ${questionCount.size} local questions to the 'mcq_questions' Firestore collection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when (val sync = syncState) {
                            is FirebaseSyncResult.Success -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = sync.message,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                }
                            }
                            is FirebaseSyncResult.Error -> {
                                Text(
                                    text = sync.errorMessage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {}
                        }

                        FilledTonalButton(
                            onClick = { viewModel.backupQuestionsToCloud() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_backup_questions"),
                            enabled = syncState !is FirebaseSyncResult.InProgress,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (syncState is FirebaseSyncResult.InProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Uploading Questions...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Backup ${questionCount.size} Questions to Cloud")
                            }
                        }
                    }
                }

                // How to Verify Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "How to see this in Firebase Console:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "1. Open console.firebase.google.com\n" +
                                   "2. Go to 'Firestore Database'\n" +
                                   "3. Check 'test_connections' collection for live pings, and 'mcq_questions' collection for synced questions.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.resetFirebaseTestState()
                    viewModel.resetFirebaseSyncState()
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_close_firebase_dialog")
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
