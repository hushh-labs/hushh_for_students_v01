package com.example.project_gemini

// QuestionCategory.kt

data class QuestionCategory(
    val questions: Array<String>,
    val optionsProvider: (questionIndex: Int) -> Array<String>
)



