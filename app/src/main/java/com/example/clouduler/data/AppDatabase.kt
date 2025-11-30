package com.example.clouduler.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/* AppDatabase
 * - Clouduler에서 사용하는 Room DB의 메인 클래스
 * - 앱 전체에서 단 하나만 존재하는 DB 인스턴스 관리
 *
 * 포함 Entity:
 *  - SubjectEntity      : 과목(시험) 정보
 *  - StudyRecordEntity  : 각 과목별 공부 기록(날짜·시간)
 */
@Database(entities = [SubjectEntity::class, StudyRecordEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    // 각 엔티티에 접근하기 위한 DAO 추상 메서드
    abstract fun subjectDao(): SubjectDao
    abstract fun studyRecordDao(): StudyRecordDao

    companion object {
        // Volatile:
        // - 여러 스레드에서 INSTANCE를 동시에 읽고/쓸 때
        // - 메모리 가시성을 보장하기 위해 사용.
        @Volatile private var INSTANCE: AppDatabase? = null

        // getDatabase
        // - 앱 전역에서 동일한 AppDatabase 인스턴스 얻기 위한 함수
        fun getDatabase(context: Context): AppDatabase {
            // 이미 생성된 INSTANCE가 있다면 그대로 반환
            return INSTANCE ?: synchronized(this) {

                // 없으면 여기서 새로 DB 인스턴스 생성
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clouduler.db" // 실제 디스크에 생성될 DB 파일
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance // 전역 INSTANCE에 저장해두고
                instance// 생성된 인스턴스 반환
            }
        }
    }
}
