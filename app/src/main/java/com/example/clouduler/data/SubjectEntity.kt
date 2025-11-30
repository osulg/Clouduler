package com.example.clouduler.data

import androidx.annotation.ColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey

/* SubjectEntity
 * - 과목 또는 시험 정보를 저장하는 Room Entity
 */
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val examDate: String,
    val difficulty: Float,
    val importance: Float,
    @ColorInt val color: Int
)
