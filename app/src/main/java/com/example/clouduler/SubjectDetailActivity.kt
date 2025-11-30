package com.example.clouduler

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.StudyRecordAdapter
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/* DailyRecord
 * - RecyclerView에 표시할 하루 기준 공부 기록
 * - date: yyyy-mm-dd
 * - totalTime: 해당 날짜 동안의 총 공부 시간
 */
data class DailyRecord(
    val date: String,
    val totalTime: Long
)

/* SubjectDetailActivity
 * - 특정 과목의 상세 정보를 보여주는 화면 (ViewActivity에서 리스트를 누르면 나옴)
 * 표시 :
 * - 과목명
 * - 시험 일까지 남은 D-Day
 * - 날짜별 학습 기록 리스트(RecyclerView)
 * - 전체 누적 공부 시간
 *
 * 동작 흐름:
 *      1) Intent로 전달된 subjectID 수신
 *      2) DB에서 과목 정보 가져옴 → 과목명 + 시험일 → D-Day 계산
 *      3) StudyRecordDao를 LiveData로 observe -> 날짜별 기록 갱신
 *      4) 리스트를 DailyRecord로 변환 -> RecyclerView 출력
 */

class SubjectDetailActivity : AppCompatActivity() {
    private lateinit var recordAdapter: StudyRecordAdapter // 날짜별 기록 표시
    private var subjectId: Int = -1 // 전달할 과목 ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_detail)

        // UI 요소
        val tvSubjectName = findViewById<TextView>(R.id.tvSubjectName)
        val tvDday = findViewById<TextView>(R.id.tvDday)
        val tvTotalStudy = findViewById<TextView>(R.id.tvTotalStudy)
        val rvRecord = findViewById<RecyclerView>(R.id.rvStudyRecords)
        val btnExit = findViewById<ImageButton>(R.id.btn_exit)

        // ----- 과목 ID 가져오기 ------ //
        // - 없으면 Activity 종료
        subjectId = intent.getIntExtra("subjectID", -1)
        if (subjectId == -1) finish()

        // ------ RecyclerView 설정 -> 날짜멸 학습 기록 ------ //
        recordAdapter = StudyRecordAdapter(emptyList())
        rvRecord.layoutManager = LinearLayoutManager(this)
        rvRecord.adapter = recordAdapter

        // ------ RoomDB 및 DAO ------ //
        val db = AppDatabase.getDatabase(this)
        val subjectDao = db.subjectDao()
        val recordDao = db.studyRecordDao()

        // ----- 과목 정보 불러오기 ----- //
        lifecycleScope.launch {
            val subject = subjectDao.getSubjectById(subjectId)

            // UI 업데이트는 메인 스레드에서
            withContext(Dispatchers.Main) {
                tvSubjectName.text = subject.name // 과목명

                // 시험날짜 기반 D-Day 계산
                val today = LocalDate.now()
                val exam = LocalDate.parse(subject.examDate)
                val diff = ChronoUnit.DAYS.between(today, exam).toInt()

                val dDayText = when {
                    diff > 0 -> "D-$diff"       // 시험이 앞으로 남았을 때
                    diff == 0 -> "D-Day"        // 시험 당일
                    else -> "D+${-diff}"        // 시험이 이미 지났을 때
                }
                tvDday.text = dDayText

                // 기록에 쓰일 과목 색상
                recordAdapter.setSubjectColor(subject.color)
            }
        }

        // 2) 학습 기록 -> LiveData로 관찰
        recordDao.getRecordBySubject(subjectId)
            .observe(this, Observer { list ->
                // 날짜별로 기록 합산
                val dailyRecord = list
                    .groupBy { it.date } // 날짜 묶기
                    .map { (date, items) ->
                        DailyRecord(
                            date = date,
                            totalTime = items.sumOf { it.studyTime } // 하루 총 공부시간(ms)
                        )
                    }
                    .sortedByDescending { it.date } // 최신 날짜가 위로

                // adapter로 갱신된 데이터 전달
                recordAdapter.updateData(dailyRecord)

                // 전체 누적 공부시간 계산 (ms -> 분)
                val totalMin = list.sumOf { it.studyTime } / 1000 / 60
                tvTotalStudy.text = "누적 공부 시간 : ${totalMin}분"
            })

        // 3) 나가기 버튼 -> Activity 종료
        btnExit.setOnClickListener { finish() }
    }
}
