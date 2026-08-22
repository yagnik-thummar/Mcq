package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DifficultyLevel
import com.example.data.QuestionEntity
import com.example.data.SubjectEnum
import com.example.ui.components.CustomTopBar
import com.example.ui.components.EditQuestionDialog
import com.example.ui.components.FormattedLatexText
import com.example.ui.viewmodel.McqViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    viewModel: McqViewModel,
    onBack: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToPaperConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredQuestions by viewModel.filteredQuestions.collectAsStateWithLifecycle()
    val rawQuestions by viewModel.rawQuestions.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedQuestionIds.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterSubject by viewModel.filterSubject.collectAsStateWithLifecycle()
    val filterDifficulty by viewModel.filterDifficulty.collectAsStateWithLifecycle()
    val filterHasDiagram by viewModel.filterHasDiagram.collectAsStateWithLifecycle()

    var editingQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Question Bank (${rawQuestions.size})",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onNavigateToOcr) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan OCR")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_question")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Question")
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedIds.size} Questions Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ready to generate exam paper",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.clearSelections() }) {
                                Text("Clear")
                            }

                            Button(
                                onClick = onNavigateToPaperConfig,
                                modifier = Modifier.testTag("bottom_bar_create_paper")
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Paper")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search formulas, chapters, topics or tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("bank_search_field"),
                singleLine = true
            )

            // Subject Filter Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterSubject == null,
                    onClick = { viewModel.filterSubject.value = null },
                    label = { Text("All Subjects") }
                )

                SubjectEnum.entries.forEach { subj ->
                    FilterChip(
                        selected = filterSubject == subj,
                        onClick = {
                            viewModel.filterSubject.value = if (filterSubject == subj) null else subj
                        },
                        label = { Text(subj.displayName) }
                    )
                }
            }

            // Secondary Filter Ribbon (Difficulty & Diagram toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DifficultyLevel.entries.forEach { diff ->
                    FilterChip(
                        selected = filterDifficulty == diff,
                        onClick = {
                            viewModel.filterDifficulty.value = if (filterDifficulty == diff) null else diff
                        },
                        label = { Text(diff.displayName) }
                    )
                }

                FilterChip(
                    selected = filterHasDiagram,
                    onClick = { viewModel.filterHasDiagram.value = !filterHasDiagram },
                    label = { Text("Has Diagram") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }

            // Select All row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredQuestions.size} questions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    TextButton(onClick = { viewModel.selectAllQuestions(true) }) {
                        Text("Select All", fontSize = 12.sp)
                    }
                    if (selectedIds.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearSelections() }) {
                            Text("Deselect", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Question List
            if (filteredQuestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No questions found matching your filter", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Try clearing filters or adding questions via OCR scanner", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredQuestions, key = { it.id }) { question ->
                        QuestionBankItemCard(
                            question = question,
                            isSelected = selectedIds.contains(question.id),
                            onToggleSelection = { viewModel.toggleQuestionSelection(question.id) },
                            onEdit = { editingQuestion = question },
                            onDuplicate = { viewModel.duplicateQuestion(question) },
                            onDelete = { viewModel.deleteQuestion(question) }
                        )
                    }
                }
            }
        }
    }

    // Edit Question Dialog
    if (editingQuestion != null) {
        EditQuestionDialog(
            question = editingQuestion,
            onDismiss = { editingQuestion = null },
            onSave = { updated ->
                viewModel.saveQuestion(updated) {
                    editingQuestion = null
                }
            }
        )
    }

    // Add New Question Dialog
    if (showAddDialog) {
        EditQuestionDialog(
            question = null,
            onDismiss = { showAddDialog = false },
            onSave = { newQ ->
                viewModel.saveQuestion(newQ) {
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun QuestionBankItemCard(
    question: QuestionEntity,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedSolution by remember { mutableStateOf(false) }

    val subjectColor = when (question.subject.uppercase()) {
        "MATHEMATICS" -> Color(0xFF6366F1)
        "CHEMISTRY" -> Color(0xFF06B6D4)
        "PHYSICS" -> Color(0xFFF59E0B)
        "BIOLOGY" -> Color(0xFF10B981)
        else -> Color(0xFF8B5CF6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggleSelection),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Selection Checkbox, Subject & Difficulty Badges, Marks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection() },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = question.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = question.difficulty,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${question.marks} Marks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Question Text with Formatted LaTeX
            FormattedLatexText(
                text = question.questionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Diagram thumbnail (if present)
            if (!question.diagramPath.isNullOrBlank()) {
                val file = File(question.diagramPath)
                if (file.exists()) {
                    val bmp = remember(question.diagramPath) { BitmapFactory.decodeFile(file.absolutePath) }
                    if (bmp != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Question diagram",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Attached Diagram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Included in PDF print layout", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Options Matrix
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OptionRow(label = "A", text = question.optionA, isCorrect = question.correctOption.equals("A", true))
                OptionRow(label = "B", text = question.optionB, isCorrect = question.correctOption.equals("B", true))
                OptionRow(label = "C", text = question.optionC, isCorrect = question.correctOption.equals("C", true))
                OptionRow(label = "D", text = question.optionD, isCorrect = question.correctOption.equals("D", true))
            }

            // Expandable Solution
            if (question.solutionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expandedSolution = !expandedSolution }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step-by-step Solution",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        if (expandedSolution) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = expandedSolution) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            FormattedLatexText(text = question.solutionText, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter / Topic and Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${question.chapter} • ${question.topic}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OptionRow(label: String, text: String, isCorrect: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isCorrect) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isCorrect) Color(0xFF047857) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        FormattedLatexText(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isCorrect) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (isCorrect) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Correct",
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF047857)
            )
        }
    }
}
