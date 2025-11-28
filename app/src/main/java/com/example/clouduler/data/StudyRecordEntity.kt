package com.example.clouduler.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_record")
data class StudyRecordEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: Int?,
    val date: String,
    val studyTime: Long

)