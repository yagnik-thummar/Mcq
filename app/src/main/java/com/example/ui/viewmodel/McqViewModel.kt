package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiOcrService
import com.example.ai.OcrExtractionResult
import com.example.data.AppDatabase
import com.example.data.DifficultyLevel
import com.example.data.GeneratedPaperEntity
import com.example.data.QuestionEntity
import com.example.data.QuestionRepository
import com.example.data.SampleDataGenerator
import com.example.data.SubjectEnum
import com.example.pdf.PdfGeneratorService
import com.example.pdf.PdfPreviewManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class OcrUiState {
    object Idle : OcrUiState()
    object Processing : OcrUiState()
    data class Success(val result: OcrExtractionResult) : OcrUiState()
    data class Error(val message: String) : OcrUiState()
}

sealed class PaperGenerationState {
    object Idle : PaperGenerationState()
    object Generating : PaperGenerationState()
    data class Success(val paper: GeneratedPaperEntity, val pdfPath: String, val solutionPdfPath: String) : PaperGenerationState()
    data class Error(val message: String) : PaperGenerationState()
}

class McqViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = QuestionRepository(database.questionDao(), database.paperDao())
    private val ocrService = GeminiOcrService()
    private val pdfGenerator = PdfGeneratorService(application)
    val pdfPreviewManager = PdfPreviewManager(application)

    // Theme state
    val appThemeMode = MutableStateFlow(AppThemeMode.INDIGO_SLATE)

    // Raw streams from DB
    val rawQuestions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawPapers: StateFlow<List<GeneratedPaperEntity>> = repository.allPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtering State
    val searchQuery = MutableStateFlow("")
    val filterSubject = MutableStateFlow<SubjectEnum?>(null)
    val filterDifficulty = MutableStateFlow<DifficultyLevel?>(null)
    val filterHasDiagram = MutableStateFlow(false)

    // Filtered Question list
    val filteredQuestions: StateFlow<List<QuestionEntity>> = combine(
        rawQuestions,
        searchQuery,
        filterSubject,
        filterDifficulty,
        filterHasDiagram
    ) { questions, query, subject, difficulty, hasDiagram ->
        questions.filter { q ->
            val matchesQuery = query.isBlank() ||
                q.questionText.contains(query, ignoreCase = true) ||
                q.chapter.contains(query, ignoreCase = true) ||
                q.topic.contains(query, ignoreCase = true) ||
                q.tags.contains(query, ignoreCase = true)

            val matchesSubject = subject == null || q.subject.equals(subject.name, ignoreCase = true)
            val matchesDifficulty = difficulty == null || q.difficulty.equals(difficulty.name, ignoreCase = true)
            val matchesDiagram = !hasDiagram || !q.diagramPath.isNullOrBlank()

            matchesQuery && matchesSubject && matchesDifficulty && matchesDiagram
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection State for Paper Building
    private val _selectedQuestionIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedQuestionIds: StateFlow<Set<Long>> = _selectedQuestionIds.asStateFlow()

    // OCR Ingestion Flow State
    private val _ocrState = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val ocrState: StateFlow<OcrUiState> = _ocrState.asStateFlow()

    val activeCroppedBitmap = MutableStateFlow<Bitmap?>(null)
    val activeDiagramPath = MutableStateFlow<String?>(null)
    val activeDiagramBitmap = MutableStateFlow<Bitmap?>(null)

    // Paper Generation Form State
    val instituteName = MutableStateFlow("Apex Institute of Science & Technology")
    val instituteSubtitle = MutableStateFlow("Department of Examination & Assessment")
    val examTitle = MutableStateFlow("Joint Entrance Mock Test - 2026")
    val subjectCode = MutableStateFlow("STEM-JEE-101")
    val gradeClass = MutableStateFlow("Grade 12 / Advanced")
    val examDate = MutableStateFlow("2026-08-25")
    val durationMinutes = MutableStateFlow(180)
    val layoutMode = MutableStateFlow("TWO_COLUMN") // "ONE_COLUMN" or "TWO_COLUMN"
    val instructions = MutableStateFlow(
        "1. All questions are compulsory multiple-choice questions (MCQs).\n" +
        "2. Each correct answer awards +4 marks, incorrect answer deducts -1 mark.\n" +
        "3. Use of scientific calculators, mobile phones, or smart devices is strictly prohibited.\n" +
        "4. Rough work can be done only on the blank margins of the question paper."
    )

    private val _paperGenState = MutableStateFlow<PaperGenerationState>(PaperGenerationState.Idle)
    val paperGenState: StateFlow<PaperGenerationState> = _paperGenState.asStateFlow()

    // PDF Preview State
    val previewPaper = MutableStateFlow<GeneratedPaperEntity?>(null)
    val previewQuestionPdfPath = MutableStateFlow<String?>(null)
    val previewSolutionPdfPath = MutableStateFlow<String?>(null)
    val previewQuestionBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val previewSolutionBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val isRenderingPreview = MutableStateFlow(false)

    init {
        // Pre-populate DB if empty
        viewModelScope.launch {
            repository.allQuestions.collect { list ->
                if (list.isEmpty()) {
                    val samples = SampleDataGenerator.getSampleQuestions()
                    database.questionDao().insertQuestions(samples)
                }
            }
        }
    }

    fun toggleQuestionSelection(id: Long) {
        val current = _selectedQuestionIds.value
        _selectedQuestionIds.value = if (current.contains(id)) current - id else current + id
    }

    fun selectAllQuestions(select: Boolean) {
        _selectedQuestionIds.value = if (select) {
            filteredQuestions.value.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    fun clearSelections() {
        _selectedQuestionIds.value = emptySet()
    }

    fun smartSelectQuestions(count: Int) {
        val all = rawQuestions.value
        val selected = all.shuffled().take(count).map { it.id }.toSet()
        _selectedQuestionIds.value = selected
    }

    // OCR Actions
    fun processOcr(bitmap: Bitmap, subjectHint: String = "Auto Detect") {
        viewModelScope.launch {
            _ocrState.value = OcrUiState.Processing
            try {
                val result = ocrService.extractQuestionFromImage(bitmap, subjectHint)
                if (result.isSuccess) {
                    _ocrState.value = OcrUiState.Success(result)
                } else {
                    _ocrState.value = OcrUiState.Error(result.errorMessage ?: "OCR extraction failed.")
                }
            } catch (e: Exception) {
                _ocrState.value = OcrUiState.Error(e.localizedMessage ?: "OCR processing error")
            }
        }
    }

    fun resetOcrState() {
        _ocrState.value = OcrUiState.Idle
        activeCroppedBitmap.value = null
        activeDiagramBitmap.value = null
        activeDiagramPath.value = null
    }

    // CRUD for Questions
    fun saveQuestion(
        question: QuestionEntity,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (question.id == 0L) {
                repository.insertQuestion(question)
            } else {
                repository.updateQuestion(question)
            }
            onComplete?.invoke()
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
            _selectedQuestionIds.value = _selectedQuestionIds.value - question.id
        }
    }

    fun duplicateQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val duplicate = question.copy(
                id = 0,
                questionText = question.questionText + " (Copy)",
                createdAt = System.currentTimeMillis()
            )
            repository.insertQuestion(duplicate)
        }
    }

    // Paper Generation Action
    fun generatePaper(onComplete: ((GeneratedPaperEntity) -> Unit)? = null) {
        viewModelScope.launch {
            _paperGenState.value = PaperGenerationState.Generating

            val selectedIds = _selectedQuestionIds.value.toList()
            val questions = if (selectedIds.isNotEmpty()) {
                repository.getQuestionsByIds(selectedIds)
            } else {
                // If nothing selected, grab all or top 10
                rawQuestions.value.take(10)
            }

            if (questions.isEmpty()) {
                _paperGenState.value = PaperGenerationState.Error("Please select at least 1 question to generate a paper.")
                return@launch
            }

            val totalMarksCalc = questions.sumOf { it.marks }

            var newPaper = GeneratedPaperEntity(
                title = examTitle.value.ifBlank { "STEM Assessment Examination" },
                instituteName = instituteName.value.ifBlank { "Apex Institute of Science" },
                instituteSubtitle = instituteSubtitle.value,
                subjectCode = subjectCode.value,
                gradeClass = gradeClass.value,
                examDate = examDate.value,
                durationMinutes = durationMinutes.value,
                totalMarks = totalMarksCalc,
                layoutMode = layoutMode.value,
                instructions = instructions.value,
                questionIdsCsv = questions.joinToString(",") { it.id.toString() },
                questionCount = questions.size,
                createdAt = System.currentTimeMillis()
            )

            try {
                // Generate Question Paper PDF
                val questionPdfPath = pdfGenerator.generateQuestionPaperPdf(newPaper, questions)
                // Generate Solution Key PDF
                val solutionPdfPath = pdfGenerator.generateSolutionKeyPdf(newPaper, questions)

                newPaper = newPaper.copy(
                    pdfFilePath = questionPdfPath,
                    solutionPdfFilePath = solutionPdfPath
                )

                val paperId = repository.insertPaper(newPaper)
                val finalPaper = newPaper.copy(id = paperId)

                _paperGenState.value = PaperGenerationState.Success(finalPaper, questionPdfPath, solutionPdfPath)
                loadPdfForPreview(finalPaper)
                onComplete?.invoke(finalPaper)
            } catch (e: Exception) {
                _paperGenState.value = PaperGenerationState.Error(e.localizedMessage ?: "Failed to generate PDF")
            }
        }
    }

    fun loadPdfForPreview(paper: GeneratedPaperEntity) {
        previewPaper.value = paper
        previewQuestionPdfPath.value = paper.pdfFilePath
        previewSolutionPdfPath.value = paper.solutionPdfFilePath

        viewModelScope.launch {
            isRenderingPreview.value = true
            val qBitmaps = paper.pdfFilePath?.let { pdfPreviewManager.renderPdfPagesToBitmaps(it) } ?: emptyList()
            val solBitmaps = paper.solutionPdfFilePath?.let { pdfPreviewManager.renderPdfPagesToBitmaps(it) } ?: emptyList()

            previewQuestionBitmaps.value = qBitmaps
            previewSolutionBitmaps.value = solBitmaps
            isRenderingPreview.value = false
        }
    }

    fun deletePaper(paper: GeneratedPaperEntity) {
        viewModelScope.launch {
            repository.deletePaper(paper)
        }
    }
}
