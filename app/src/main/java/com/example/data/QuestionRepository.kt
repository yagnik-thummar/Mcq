package com.example.data

import kotlinx.coroutines.flow.Flow

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val paperDao: PaperDao
) {
    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val allPapers: Flow<List<GeneratedPaperEntity>> = paperDao.getAllPapers()
    val questionCount: Flow<Int> = questionDao.getQuestionCount()
    val paperCount: Flow<Int> = paperDao.getPaperCount()

    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsBySubject(subject)

    fun getQuestionsByDifficulty(difficulty: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsByDifficulty(difficulty)

    fun searchQuestions(query: String): Flow<List<QuestionEntity>> =
        questionDao.searchQuestions(query)

    suspend fun getQuestionById(id: Long): QuestionEntity? =
        questionDao.getQuestionById(id)

    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity> =
        questionDao.getQuestionsByIds(ids)

    suspend fun insertQuestion(question: QuestionEntity): Long =
        questionDao.insertQuestion(question)

    suspend fun updateQuestion(question: QuestionEntity) =
        questionDao.updateQuestion(question)

    suspend fun deleteQuestion(question: QuestionEntity) =
        questionDao.deleteQuestion(question)

    suspend fun deleteQuestionById(id: Long) =
        questionDao.deleteQuestionById(id)

    suspend fun insertPaper(paper: GeneratedPaperEntity): Long =
        paperDao.insertPaper(paper)

    suspend fun getPaperById(id: Long): GeneratedPaperEntity? =
        paperDao.getPaperById(id)

    suspend fun deletePaper(paper: GeneratedPaperEntity) =
        paperDao.deletePaper(paper)

    suspend fun deletePaperById(id: Long) =
        paperDao.deletePaperById(id)
}
