package com.example.clouduler.data

import androidx.annotation.ColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey

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
