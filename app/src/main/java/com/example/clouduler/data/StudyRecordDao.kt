package com.example.clouduler.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/* StudyRecordDao
 * - StudyRecordEntity(공부기록) 테이블에 접근하는 DAO
 * - 1일 단위의 공부 기록을 저장/조회/업데이트
 */
@Dao
interface StudyRecordDao {
    // insertRecord
    // - 새로운 공부 기록 추가
    @Insert
    suspend fun insertRecord(subject: StudyRecordEntity)

    // getRecordsByDate
    // - 특정 날짜에 기록된 모든 리스트
    // - LiveData -> UI에서 observe
    @Query("SELECT * FROM study_record WHERE date = :date")
    fun getRecordsByDate(date: String): LiveData<List<StudyRecordEntity>>

    // getTotalStudyTime
    // - 특정 과목의 전체 누적 공부 시간
    @Query("SELECT SUM(studyTime) FROM study_record WHERE subjectId = :subjectId")
    suspend fun getTotalStudyTime(subjectId: Int): Long?

    // getRecordSubject
    // - 특정 과목의 모든 공부 시간을 날짜 내림차순 반환
    @Query("SELECT * FROM study_record WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getRecordBySubject(subjectId: Int): LiveData<List<StudyRecordEntity>>

    // updateRecord
    // - 기록을 수정할 때
    @Update
    suspend fun updateRecord(record: StudyRecordEntity)

    // findRecord
    // - 특정 과목 + 특정 날짜 조합에 해당하는 기록 가져오기
    @Query("SELECT * FROM study_record WHERE subjectId = :subjectId AND date = :date LIMIT 1")
    suspend fun findRecord(subjectId: Int, date: String): StudyRecordEntity?

    // insertOrUpdate
    // - findRecord -> 동일 과목 + 동일 날짜의 기록이 존재하는지 확인
    // - 존재하지 않으면 -> 새로운 기록 넣기 (insertRecord)
    // - 존재 -> 기존 studyTime에 새로운 studyTime 더하여 업데이트
    suspend fun insertOrUpdate(record: StudyRecordEntity){
        // 오늘에 대한 기존 기록 존재하는지 확인       
        val existingTime = findRecord(record.subjectId!!, record.date)

        // 기록이 없으면 업데이트
        if(existingTime == null){
            insertRecord(record)
        }
        // 있으면 -> 기존 기록에 새 기록 더하기
        else{
            val updatedTime = existingTime.copy(
                studyTime = existingTime.studyTime + record.studyTime
            )
            updateRecord(updatedTime)
        }
    }

}
