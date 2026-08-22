package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DifficultyLevel
import com.example.data.QuestionEntity
import com.example.data.SubjectEnum
import com.example.ui.components.CustomTopBar
import com.example.ui.components.DemoExamSheets
import com.example.ui.components.DiagramCropper
import com.example.ui.components.FormattedLatexText
import com.example.ui.components.MathKeyboardToolbar
import com.example.ui.viewmodel.McqViewModel
import com.example.ui.viewmodel.OcrUiState

@Composable
fun OcrScannerScreen(
    viewModel: McqViewModel,
    onBack: () -> Unit,
    onQuestionSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ocrState by viewModel.ocrState.collectAsStateWithLifecycle()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(DemoExamSheets.createMathSampleSheet()) }
    var selectedPresetName by remember { mutableStateOf("Calculus Exam Sheet") }
    var activeSubjectHint by remember { mutableStateOf("Mathematics") }

    // Extracted Fields for Review Form
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctOption by remember { mutableStateOf("A") }
    var solutionText by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(SubjectEnum.MATHEMATICS.name) }
    var chapter by remember { mutableStateOf("Integral Calculus") }
    var topic by remember { mutableStateOf("Definite Integrals") }
    var difficulty by remember { mutableStateOf(DifficultyLevel.MEDIUM.name) }
    var tags by remember { mutableStateOf("JEE,Exam,Calculus") }
    var marks by remember { mutableStateOf("4") }
    var diagramPath by remember { mutableStateOf<String?>(null) }
    var diagramBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var showLatexPreview by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf(false) }

    // Image Picker from Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                sourceBitmap = bitmap
                selectedPresetName = "Gallery Photo"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            sourceBitmap = bitmap
            selectedPresetName = "Camera Photo"
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to launch camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture question papers directly", Toast.LENGTH_SHORT).show()
        }
    }

    val openCameraSafely = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to launch camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Update form when OCR completes
    androidx.compose.runtime.LaunchedEffect(ocrState) {
        if (ocrState is OcrUiState.Success) {
            val result = (ocrState as OcrUiState.Success).result
            questionText = result.questionText
            optionA = result.optionA
            optionB = result.optionB
            optionC = result.optionC
            optionD = result.optionD
            correctOption = result.correctOption
            solutionText = result.solutionText
            subject = result.subject
            chapter = result.chapter
            topic = result.topic
            difficulty = result.difficulty
            tags = result.tags
            marks = result.marks.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(
            title = "OCR Question Ingestion",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Input Source Selector
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step 1: Choose Source Exam Page",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Active: $selectedPresetName",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Source buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { openCameraSafely() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Or load sample high-school/JEE exam page:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedPresetName == "Calculus Exam Sheet",
                                onClick = {
                                    sourceBitmap = DemoExamSheets.createMathSampleSheet()
                                    selectedPresetName = "Calculus Exam Sheet"
                                    activeSubjectHint = "Mathematics"
                                },
                                label = { Text("Math Calculus Sheet", fontSize = 12.sp) }
                            )

                            FilterChip(
                                selected = selectedPresetName == "Organic Chemistry Sheet",
                                onClick = {
                                    sourceBitmap = DemoExamSheets.createChemSampleSheet()
                                    selectedPresetName = "Organic Chemistry Sheet"
                                    activeSubjectHint = "Chemistry"
                                },
                                label = { Text("Organic Chem Sheet", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Step 2: Interactive Cropper
            if (sourceBitmap != null) {
                item {
                    DiagramCropper(
                        sourceBitmap = sourceBitmap!!,
                        onQuestionCropped = { croppedBitmap ->
                            viewModel.processOcr(croppedBitmap, activeSubjectHint)
                        },
                        onDiagramExtracted = { path, bmp ->
                            diagramPath = path
                            diagramBitmap = bmp
                        }
                    )
                }
            }

            // Step 3: OCR Progress or Error
            when (ocrState) {
                is OcrUiState.Processing -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "AI Multimodal OCR in progress...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Extracting LaTeX formulas, chemistry bonds, options & solution",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                is OcrUiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "OCR Notice",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = (ocrState as OcrUiState.Error).message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }

            // Step 4: Extracted Question Review & LaTeX Editor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step 2: Review & Format LaTeX",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (questionText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "✓ Extracted",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Math Keyboard Toolbar
                        MathKeyboardToolbar(
                            onInsertSymbol = { sym ->
                                questionText += sym
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            label = { Text("Question Text (LaTeX $$...$$ supported)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ocr_question_text"),
                            minLines = 3,
                            maxLines = 6
                        )

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
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Live LaTeX Preview:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FormattedLatexText(questionText, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Diagram attachment preview
                        if (diagramBitmap != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = diagramBitmap!!.asImageBitmap(),
                                    contentDescription = "Attached diagram",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Attached Diagram", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Will be rendered in question PDF", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = {
                                    diagramBitmap = null
                                    diagramPath = null
                                }) {
                                    Text("Remove", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Options A, B, C, D
                        Text("Options (Select Correct Answer):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        val optionsList = listOf(
                            Triple("A", optionA, { v: String -> optionA = v }),
                            Triple("B", optionB, { v: String -> optionB = v }),
                            Triple("C", optionC, { v: String -> optionC = v }),
                            Triple("D", optionD, { v: String -> optionD = v })
                        )

                        optionsList.forEach { (label, value, onValChange) ->
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

                        // Solution / Explanation
                        OutlinedTextField(
                            value = solutionText,
                            onValueChange = { solutionText = it },
                            label = { Text("Step-by-Step Solution & Working (LaTeX supported)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metadata Tagging
                        Text("Metadata & Classification:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Subject chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // Difficulty chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
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

                        Spacer(modifier = Modifier.height(10.dp))

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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save Button
                        Button(
                            onClick = {
                                val questionEntity = QuestionEntity(
                                    subject = subject,
                                    chapter = chapter,
                                    topic = topic,
                                    difficulty = difficulty,
                                    tags = tags,
                                    questionText = questionText,
                                    diagramPath = diagramPath,
                                    optionA = optionA,
                                    optionB = optionB,
                                    optionC = optionC,
                                    optionD = optionD,
                                    correctOption = correctOption,
                                    solutionText = solutionText,
                                    marks = marks.toIntOrNull() ?: 4
                                )
                                viewModel.saveQuestion(questionEntity) {
                                    saveSuccessMessage = true
                                    onQuestionSaved()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_question_button"),
                            enabled = questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save to Question Bank", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        if (saveSuccessMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Question successfully saved to database!",
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
