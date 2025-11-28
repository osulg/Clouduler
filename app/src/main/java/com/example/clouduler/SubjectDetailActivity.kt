package com.example.clouduler

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

data class DailyRecord(
    val date: String,
    val totalTime: Long
)

class SubjectDetailActivity : AppCompatActivity() {
    private lateinit var recordAdapter: StudyRecordAdapter
    private var subjectId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_detail)

        val tvSubjectName = findViewById<TextView>(R.id.tvSubjectName)
        val tvDday = findViewById<TextView>(R.id.tvDday)
        val tvTotalStudy = findViewById<TextView>(R.id.tvTotalStudy)
        val rvRecord = findViewById<RecyclerView>(R.id.rvStudyRecords)
        val btnExit = findViewById<ImageButton>(R.id.btn_exit)

        subjectId = intent.getIntExtra("subjectID", -1)
        if (subjectId == -1) finish()

        recordAdapter = StudyRecordAdapter(emptyList())
        rvRecord.layoutManager = LinearLayoutManager(this)
        rvRecord.adapter = recordAdapter

        val db = AppDatabase.getDatabase(this)
        val subjectDao = db.subjectDao()
        val recordDao = db.studyRecordDao()

        lifecycleScope.launch {
            val subject = subjectDao.getSubjectById(subjectId)
            val totalTime = recordDao.getTotalStudyTime(subjectId) ?: 0
            val record = recordDao.getRecordBySubject(subjectId)

            // 날짜별 묶음
            val dailyRecords = record
                .groupBy { it.date }
                .map { (date, list) ->
                    DailyRecord(
                        date = date,
                        totalTime = list.sumOf { it.studyTime }
                    )
                }
                .sortedByDescending { it.date }

            withContext(Dispatchers.Main) {
                tvSubjectName.text = subject.name

                val date = LocalDate.parse(subject.examDate)
                val dday = ChronoUnit.DAYS.between(LocalDate.now(), date)
                tvDday.text = "D-$dday"

                val minutes = totalTime / 1000 / 60
                tvTotalStudy.text = "누적 공부시간: ${minutes}분"

                // ⭐ 과목 색상 전달
                recordAdapter.setSubjectColor(subject.color)

                // ⭐ 날짜별 기록 업데이트
                recordAdapter.updateData(dailyRecords)
            }
        }

        btnExit.setOnClickListener { finish() }
    }
}
