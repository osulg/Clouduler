package com.example.clouduler.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudyRecordDao {
    @Insert
    suspend fun insertRecord(subject: StudyRecordEntity)

    @Query("SELECT * FROM study_record WHERE date = :date")
    fun getRecordsByDate(date: String): LiveData<List<StudyRecordEntity>>

    @Query("SELECT SUM(studyTime) FROM study_record WHERE subjectId = :subjectId")
    suspend fun getTotalStudyTime(subjectId: Int): Long?

    @Query("SELECT * FROM study_record WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getRecordBySubject(subjectId: Int): LiveData<List<StudyRecordEntity>>

    @Update
    suspend fun updateRecord(record: StudyRecordEntity)

    @Query("SELECT * FROM study_record WHERE subjectId = :subjectId AND date = :date LIMIT 1")
    suspend fun findRecord(subjectId: Int, date: String): StudyRecordEntity?


    suspend fun insertOrUpdate(record: StudyRecordEntity){
        val existingTime = findRecord(record.subjectId!!, record.date)

        if(existingTime == null){
            insertRecord(record)
        }
        else{
            val updatedTime = existingTime.copy(
                studyTime = existingTime.studyTime + record.studyTime
            )
            updateRecord(updatedTime)
        }
    }

}
