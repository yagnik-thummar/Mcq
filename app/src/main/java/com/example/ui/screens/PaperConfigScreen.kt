package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GeneratedPaperEntity
import com.example.ui.components.CustomTopBar
import com.example.ui.components.FormattedLatexText
import com.example.ui.viewmodel.McqViewModel
import com.example.ui.viewmodel.PaperGenerationState

@Composable
fun PaperConfigScreen(
    viewModel: McqViewModel,
    onBack: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToPdfPreview: (GeneratedPaperEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val instituteName by viewModel.instituteName.collectAsStateWithLifecycle()
    val instituteSubtitle by viewModel.instituteSubtitle.collectAsStateWithLifecycle()
    val examTitle by viewModel.examTitle.collectAsStateWithLifecycle()
    val subjectCode by viewModel.subjectCode.collectAsStateWithLifecycle()
    val gradeClass by viewModel.gradeClass.collectAsStateWithLifecycle()
    val examDate by viewModel.examDate.collectAsStateWithLifecycle()
    val durationMinutes by viewModel.durationMinutes.collectAsStateWithLifecycle()
    val layoutMode by viewModel.layoutMode.collectAsStateWithLifecycle()
    val instructions by viewModel.instructions.collectAsStateWithLifecycle()

    val rawQuestions by viewModel.rawQuestions.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedQuestionIds.collectAsStateWithLifecycle()
    val paperGenState by viewModel.paperGenState.collectAsStateWithLifecycle()

    val selectedQuestions = rawQuestions.filter { selectedIds.contains(it.id) }
    val totalCalculatedMarks = selectedQuestions.sumOf { it.marks }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(
            title = "Exam Paper Configuration",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Layout Mode Selector (1-Col vs 2-Col)
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Page Layout Style",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 2-Column Option
                            LayoutOptionCard(
                                title = "2-Column Exam",
                                subtitle = "Classic dense format with center divider line (JEE/SAT style)",
                                icon = Icons.Default.ViewColumn,
                                isSelected = layoutMode == "TWO_COLUMN",
                                onClick = { viewModel.layoutMode.value = "TWO_COLUMN" },
                                modifier = Modifier.weight(1f)
                            )

                            // 1-Column Option
                            LayoutOptionCard(
                                title = "1-Column Spacious",
                                subtitle = "Full-width questions, ideal for large diagrams & step proofs",
                                icon = Icons.Default.ViewAgenda,
                                isSelected = layoutMode == "ONE_COLUMN",
                                onClick = { viewModel.layoutMode.value = "ONE_COLUMN" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Institute & Header Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Institute & Header Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = instituteName,
                            onValueChange = { viewModel.instituteName.value = it },
                            label = { Text("Institute / School / University Name") },
                            modifier = Modifier.fillMaxWidth().testTag("institute_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = instituteSubtitle,
                            onValueChange = { viewModel.instituteSubtitle.value = it },
                            label = { Text("Department / Exam Board") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = examTitle,
                            onValueChange = { viewModel.examTitle.value = it },
                            label = { Text("Exam Title (e.g. JEE Mock Assessment)") },
                            modifier = Modifier.fillMaxWidth().testTag("exam_title_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = subjectCode,
                                onValueChange = { viewModel.subjectCode.value = it },
                                label = { Text("Subject Code") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = gradeClass,
                                onValueChange = { viewModel.gradeClass.value = it },
                                label = { Text("Class / Batch") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = examDate,
                                onValueChange = { viewModel.examDate.value = it },
                                label = { Text("Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = durationMinutes.toString(),
                                onValueChange = { viewModel.durationMinutes.value = it.toIntOrNull() ?: 180 },
                                label = { Text("Duration (Mins)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // General Instructions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "General Instructions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.instructions.value =
                                        "1. All questions are compulsory multiple-choice questions (MCQs).\n" +
                                        "2. Each correct answer awards +4 marks, incorrect answer deducts -1 mark.\n" +
                                        "3. Use of scientific calculators or mobile phones is strictly prohibited."
                                },
                                label = { Text("JEE Standard (+4, -1)", fontSize = 11.5.sp) }
                            )

                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.instructions.value =
                                        "1. This question paper comprises 20 MCQs with single correct option.\n" +
                                        "2. No negative marking for wrong answers.\n" +
                                        "3. Darken only one circle per question on the OMR sheet."
                                },
                                label = { Text("Board Exam (No Negatives)", fontSize = 11.5.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { viewModel.instructions.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                }
            }

            // Question Selection Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Selected Questions (${selectedQuestions.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Total Marks: $totalCalculatedMarks",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = onNavigateToBank,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pick Questions", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Pick buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.smartSelectQuestions(5) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pick 5 Qs", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.smartSelectQuestions(10) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pick 10 Qs", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.selectAllQuestions(true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pick All", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedQuestions.isEmpty()) {
                            Text(
                                text = "No questions selected. Tap 'Pick Questions' or 'Pick 10 Qs' to build the paper.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                selectedQuestions.forEachIndexed { idx, q ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Q${idx + 1}. [${q.subject}] ${q.chapter}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${q.marks}M",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.toggleQuestionSelection(q.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Generate Button
            item {
                when (paperGenState) {
                    is PaperGenerationState.Generating -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Compiling Vector A4 PDF & Solution Key...", fontWeight = FontWeight.Bold)
                        }
                    }
                    is PaperGenerationState.Error -> {
                        Text(
                            text = (paperGenState as PaperGenerationState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    else -> Unit
                }

                Button(
                    onClick = {
                        viewModel.generatePaper { generatedPaper ->
                            onNavigateToPdfPreview(generatedPaper)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_paper_pdf_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = selectedQuestions.isNotEmpty()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Generate Question Paper & Solution PDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LayoutOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}
