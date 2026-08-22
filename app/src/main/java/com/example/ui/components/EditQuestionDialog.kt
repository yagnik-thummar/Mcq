package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.DifficultyLevel
import com.example.data.QuestionEntity
import com.example.data.SubjectEnum

@Composable
fun EditQuestionDialog(
    question: QuestionEntity?,
    onDismiss: () -> Unit,
    onSave: (QuestionEntity) -> Unit
) {
    val isEdit = question != null

    var subject by remember { mutableStateOf(question?.subject ?: SubjectEnum.MATHEMATICS.name) }
    var chapter by remember { mutableStateOf(question?.chapter ?: "Calculus") }
    var topic by remember { mutableStateOf(question?.topic ?: "Integrals") }
    var difficulty by remember { mutableStateOf(question?.difficulty ?: DifficultyLevel.MEDIUM.name) }
    var tags by remember { mutableStateOf(question?.tags ?: "JEE,Exam") }

    var questionText by remember { mutableStateOf(question?.questionText ?: "") }
    var optionA by remember { mutableStateOf(question?.optionA ?: "") }
    var optionB by remember { mutableStateOf(question?.optionB ?: "") }
    var optionC by remember { mutableStateOf(question?.optionC ?: "") }
    var optionD by remember { mutableStateOf(question?.optionD ?: "") }
    var correctOption by remember { mutableStateOf(question?.correctOption ?: "A") }
    var solutionText by remember { mutableStateOf(question?.solutionText ?: "") }
    var marks by remember { mutableStateOf((question?.marks ?: 4).toString()) }

    var showLatexPreview by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(20.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Question" else "Add New Question",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Subject selection chips
                Text("Subject:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubjectEnum.entries.forEach { subj ->
                        FilterChip(
                            selected = subject.equals(subj.name, ignoreCase = true),
                            onClick = { subject = subj.name },
                            label = { Text(subj.displayName, fontSize = 12.sp) }
                        )
                    }
                }

                // Difficulty chips
                Text("Difficulty:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DifficultyLevel.entries.forEach { diff ->
                        FilterChip(
                            selected = difficulty.equals(diff.name, ignoreCase = true),
                            onClick = { difficulty = diff.name },
                            label = { Text(diff.displayName, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Math Keyboard Symbol Helper
                MathKeyboardToolbar(
                    onInsertSymbol = { sym ->
                        questionText += sym
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Question Text Field
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text (LaTeX $$...$$ supported)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                // Toggle Preview button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showLatexPreview = !showLatexPreview }) {
                        Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showLatexPreview) "Hide Preview" else "Preview LaTeX")
                    }
                }

                if (showLatexPreview && questionText.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Rendered Question:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            FormattedLatexText(questionText, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Options A, B, C, D with Radio for Correct Option
                Text("Options & Correct Answer:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val options = listOf(
                    Triple("A", optionA, { v: String -> optionA = v }),
                    Triple("B", optionB, { v: String -> optionB = v }),
                    Triple("C", optionC, { v: String -> optionC = v }),
                    Triple("D", optionD, { v: String -> optionD = v })
                )

                options.forEach { (label, value, onValChange) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = correctOption == label,
                            onClick = { correctOption = label }
                        )
                        OutlinedTextField(
                            value = value,
                            onValueChange = onValChange,
                            label = { Text("Option ($label)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Solution Field
                OutlinedTextField(
                    value = solutionText,
                    onValueChange = { solutionText = it },
                    label = { Text("Step-by-step Solution / Explanation (LaTeX supported)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chapter, Topic, Marks row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = marks,
                        onValueChange = { marks = it },
                        label = { Text("Marks") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (question ?: QuestionEntity(
                        questionText = questionText,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD
                    )).copy(
                        subject = subject,
                        chapter = chapter,
                        topic = topic,
                        difficulty = difficulty,
                        tags = tags,
                        questionText = questionText,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctOption = correctOption,
                        solutionText = solutionText,
                        marks = marks.toIntOrNull() ?: 4
                    )
                    onSave(updated)
                },
                enabled = questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Question")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
