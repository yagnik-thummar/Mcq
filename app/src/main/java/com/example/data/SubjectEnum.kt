package com.example.data

import androidx.compose.ui.graphics.Color

enum class SubjectEnum(
    val displayName: String,
    val code: String,
    val defaultColor: Long
) {
    MATHEMATICS("Mathematics", "MATH", 0xFF6366F1),
    CHEMISTRY("Chemistry", "CHEM", 0xFF06B6D4),
    PHYSICS("Physics", "PHYS", 0xFFF59E0B),
    BIOLOGY("Biology", "BIO", 0xFF10B981),
    GENERAL_SCIENCE("General Science", "SCI", 0xFF8B5CF6),
    COMPUTER_SCIENCE("Computer Science", "CS", 0xFFEC4899);

    fun getColor(): Color = Color(defaultColor)

    companion object {
        fun fromString(value: String): SubjectEnum {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: MATHEMATICS
        }
    }
}

enum class DifficultyLevel(val displayName: String, val weightColor: Long) {
    EASY("Easy", 0xFF10B981),
    MEDIUM("Medium", 0xFFF59E0B),
    HARD("Hard", 0xFFEF4444),
    JEE_ADVANCED("JEE / Advanced", 0xFF8B5CF6);

    fun getColor(): Color = Color(weightColor)

    companion object {
        fun fromString(value: String): DifficultyLevel {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: MEDIUM
        }
    }
}
