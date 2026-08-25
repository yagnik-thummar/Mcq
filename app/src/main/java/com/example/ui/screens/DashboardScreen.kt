package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GeneratedPaperEntity
import com.example.data.SubjectEnum
import com.example.ui.components.CustomTopBar
import com.example.ui.viewmodel.McqViewModel

@Composable
fun DashboardScreen(
    viewModel: McqViewModel,
    onNavigateToOcr: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToPaperConfig: () -> Unit,
    onNavigateToPdfPreview: (GeneratedPaperEntity) -> Unit,
    onThemeClick: () -> Unit,
    onFirebaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val questions by viewModel.rawQuestions.collectAsStateWithLifecycle()
    val papers by viewModel.rawPapers.collectAsStateWithLifecycle()
    val firebaseInfo by viewModel.firebaseConnectionInfo.collectAsStateWithLifecycle()

    val mathCount = questions.count { it.subject.equals(SubjectEnum.MATHEMATICS.name, ignoreCase = true) }
    val chemCount = questions.count { it.subject.equals(SubjectEnum.CHEMISTRY.name, ignoreCase = true) }
    val physCount = questions.count { it.subject.equals(SubjectEnum.PHYSICS.name, ignoreCase = true) }
    val bioCount = questions.count { it.subject.equals(SubjectEnum.BIOLOGY.name, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(
            title = "MCQ Paper Generator",
            onThemeClick = onThemeClick,
            onCloudClick = onFirebaseClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Welcome Card with Gradient
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "OCR • LaTeX • A4 PDF Engine",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Science,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Create Exam Papers in Minutes",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Scan textbook formulas & diagrams with AI OCR, render crisp LaTeX, and export 1-Column or 2-Column question papers with solution keys.",
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onNavigateToOcr,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("hero_ocr_button")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan Question (OCR)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }

                                OutlinedButton(
                                    onClick = onNavigateToPaperConfig,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("hero_create_paper_button")
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Paper", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Stats Metric Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox(
                        title = "Question Bank",
                        value = "${questions.size}",
                        subtitle = "Total MCQs",
                        icon = Icons.Default.LocalLibrary,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Papers Built",
                        value = "${papers.size}",
                        subtitle = "PDFs Generated",
                        icon = Icons.Default.PictureAsPdf,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Subject Breakdown Pills
            item {
                Column {
                    Text(
                        text = "Subject Inventory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectPill("Math", mathCount, Color(0xFF6366F1), modifier = Modifier.weight(1f))
                        SubjectPill("Chem", chemCount, Color(0xFF06B6D4), modifier = Modifier.weight(1f))
                        SubjectPill("Physics", physCount, Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        SubjectPill("Biology", bioCount, Color(0xFF10B981), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Firebase Cloud Connection Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onFirebaseClick() }
                        .testTag("dashboard_firebase_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (firebaseInfo.isInitialized)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (firebaseInfo.isInitialized)
                                            Color(0xFFE8F5E9)
                                        else
                                            MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (firebaseInfo.isInitialized) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (firebaseInfo.isInitialized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Firebase Cloud Integration",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (firebaseInfo.isInitialized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary
                                    ) {
                                        Text(
                                            text = if (firebaseInfo.isInitialized) "ACTIVE" else "READY",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (firebaseInfo.projectId != null)
                                        "Project: ${firebaseInfo.projectId} • Tap to test ping & sync"
                                    else
                                        "Tap to test connection & sync question bank",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Open Firebase Status",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Action Grid
            item {
                Column {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "OCR Camera / Cropper",
                            desc = "Snap pages, isolate diagrams & auto-convert math formulas",
                            icon = Icons.Default.CameraAlt,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = onNavigateToOcr,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_action_ocr")
                        )

                        ActionCard(
                            title = "Browse Question Bank",
                            desc = "Filter by tags & LaTeX preview with ${questions.size} stored questions",
                            icon = Icons.Default.FormatListNumbered,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = onNavigateToBank,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_action_bank")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Configure Exam Paper",
                            desc = "Set institute logo, layout mode (1 or 2 col), marks & generate PDF",
                            icon = Icons.Default.Description,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = onNavigateToPaperConfig,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_action_paper")
                        )

                        ActionCard(
                            title = "Instant 10-Q Mock Test",
                            desc = "Auto-pick 10 mixed questions & create ready-to-print A4 PDF instantly",
                            icon = Icons.Default.AutoAwesome,
                            containerColor = Color(0xFFDCFCE7),
                            contentColor = Color(0xFF166534),
                            onClick = {
                                viewModel.smartSelectQuestions(10)
                                onNavigateToPaperConfig()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_action_instant_test")
                        )
                    }
                }
            }

            // Recent Question Papers Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Question Papers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (papers.isNotEmpty()) {
                        Text(
                            text = "${papers.size} papers",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (papers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No question papers generated yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select questions from the bank or use OCR scanner to generate your first A4 PDF exam paper.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.smartSelectQuestions(8)
                                    onNavigateToPaperConfig()
                                }
                            ) {
                                Text("Generate Sample Exam Paper")
                            }
                        }
                    }
                }
            } else {
                items(papers) { paper ->
                    RecentPaperCard(
                        paper = paper,
                        onPreview = {
                            viewModel.loadPdfForPreview(paper)
                            onNavigateToPdfPreview(paper)
                        },
                        onShare = {
                            paper.pdfFilePath?.let { viewModel.pdfPreviewManager.sharePdf(context, it, paper.title) }
                        },
                        onPrint = {
                            paper.pdfFilePath?.let { viewModel.pdfPreviewManager.printPdf(context, it, paper.title) }
                        },
                        onDelete = {
                            viewModel.deletePaper(paper)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SubjectPill(name: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "$count Qs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    desc: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = contentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = contentColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 11.sp, color = contentColor.copy(alpha = 0.8f), lineHeight = 15.sp)
        }
    }
}

@Composable
fun RecentPaperCard(
    paper: GeneratedPaperEntity,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPreview),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (paper.layoutMode == "TWO_COLUMN") "2-Column Exam" else "1-Column Standard",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${paper.questionCount} Questions • ${paper.totalMarks} Marks",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = paper.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${paper.instituteName} • Code: ${paper.subjectCode}",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${paper.examDate} (${paper.durationMinutes}m)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onPrint, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
