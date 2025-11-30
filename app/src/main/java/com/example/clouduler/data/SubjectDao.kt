package com.example.clouduler.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/* SubjectDao
 * - SubjectEntity(과목/시험 정보) 테이블에 접근하는 DAO
 * - 생성/읽기/수정/삭제 기능을 제공
 */
@Dao
interface SubjectDao {
    // insertSubject
    // - 새로운 과목을 DB에 추가
    @Insert
    suspend fun insertSubject(subject: SubjectEntity)

    // getAllSubjects
    // - 전체 과목 목록을 시험 날짜 오름차순으로 반환
    // - LiveData -> UI에서 자동 observe
    @Query("SELECT * FROM subjects ORDER BY examDate ASC")
    fun getAllSubjects(): androidx.lifecycle.LiveData<List<SubjectEntity>>

    // getSubjectById
    // - 특정 과목 ID로 해당 과목 하나 가져옴
    // - LINIT 1 -> 하나의 entitiy 가져옴
    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: Int): SubjectEntity

    // delete
    // 특정 과목 삭제
    @Delete
    suspend fun delete(subject: SubjectEntity)

    // updateSubject
    // 기존 과목 정보 수정
    @Update
    suspend fun updateSubject(subject: SubjectEntity)
}
