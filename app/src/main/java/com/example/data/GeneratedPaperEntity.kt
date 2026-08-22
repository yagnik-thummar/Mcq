package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_papers")
data class GeneratedPaperEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val instituteName: String = "Apex Institute of Science & Technology",
    val instituteSubtitle: String = "Department of Examination & Assessment",
    val instituteLogoPath: String? = null,
    val subjectCode: String = "STEM-101",
    val gradeClass: String = "Class XII / JEE Advanced",
    val examDate: String = "2026-08-25",
    val durationMinutes: Int = 180,
    val totalMarks: Int = 100,
    val negativeMarkingText: String = "+4 for correct, -1 for incorrect",
    val layoutMode: String = "TWO_COLUMN", // "ONE_COLUMN" or "TWO_COLUMN"
    val instructions: String = "1. All questions are compulsory.\n2. Each question carries 4 marks with 1 mark deduction for incorrect response.\n3. Use of electronic calculators or logarithmic tables is strictly prohibited.\n4. Rough work must be done only in the designated space.",
    val questionIdsCsv: String, // comma-separated question IDs
    val pdfFilePath: String? = null,
    val solutionPdfFilePath: String? = null,
    val questionCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getQuestionIdList(): List<Long> {
        return questionIdsCsv.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }
}
