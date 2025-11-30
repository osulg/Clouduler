package com.example.clouduler.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/* StudyRecordEntity
 * - 공부 기록을 저장하는 Room Entity
 * - 1일 단위로 과목별 공부 시간을 누적 저장
 */
@Entity(tableName = "study_record")
data class StudyRecordEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: Int?,
    val date: String,
    val studyTime: Long

)