package com.example.clouduler.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SubjectDao {
    @Insert
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects ORDER BY examDate ASC")
    fun getAllSubjects(): androidx.lifecycle.LiveData<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: Int): SubjectEntity

    @Delete
    suspend fun delete(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)
}
