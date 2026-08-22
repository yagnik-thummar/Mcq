package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String = SubjectEnum.MATHEMATICS.name,
    val chapter: String = "Calculus",
    val topic: String = "Integrals",
    val difficulty: String = DifficultyLevel.MEDIUM.name,
    val tags: String = "JEE,Exam",
    val questionText: String,
    val diagramPath: String? = null,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String = "A", // "A", "B", "C", "D"
    val solutionText: String = "",
    val solutionDiagramPath: String? = null,
    val marks: Int = 4,
    val negativeMarks: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getSubjectEnum(): SubjectEnum = SubjectEnum.fromString(subject)
    fun getDifficultyEnum(): DifficultyLevel = DifficultyLevel.fromString(difficulty)
    
    fun getTagList(): List<String> {
        return tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
}
