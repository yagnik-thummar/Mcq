package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperDao {
    @Query("SELECT * FROM generated_papers ORDER BY createdAt DESC")
    fun getAllPapers(): Flow<List<GeneratedPaperEntity>>

    @Query("SELECT * FROM generated_papers WHERE id = :id")
    suspend fun getPaperById(id: Long): GeneratedPaperEntity?

    @Query("SELECT COUNT(*) FROM generated_papers")
    fun getPaperCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: GeneratedPaperEntity): Long

    @Update
    suspend fun updatePaper(paper: GeneratedPaperEntity)

    @Delete
    suspend fun deletePaper(paper: GeneratedPaperEntity)

    @Query("DELETE FROM generated_papers WHERE id = :id")
    suspend fun deletePaperById(id: Long)
}
