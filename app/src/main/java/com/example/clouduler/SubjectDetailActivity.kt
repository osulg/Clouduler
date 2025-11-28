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

            withContext(Dispatchers.Main) {
                tvSubjectName.text = subject.name

                val today = LocalDate.now()
                val exam = LocalDate.parse(subject.examDate)
                val diff = ChronoUnit.DAYS.between(today, exam).toInt()

                val dDayText = when {
                    diff > 0 -> "D-$diff"       // 시험이 앞으로 남았을 때
                    diff == 0 -> "D-Day"        // 시험 당일
                    else -> "D+${-diff}"        // 시험이 이미 지났을 때
                }
                tvDday.text = dDayText
                recordAdapter.setSubjectColor(subject.color)
            }
        }

        recordDao.getRecordBySubject(subjectId)
            .observe(this, Observer { list ->
                val dailyRecord = list
                    .groupBy { it.date }
                    .map { (date, items) ->
                        DailyRecord(
                            date = date,
                            totalTime = items.sumOf { it.studyTime }
                        )
                    }
                    .sortedByDescending { it.date }

                recordAdapter.updateData(dailyRecord)

                val totalMin = list.sumOf { it.studyTime } / 1000 / 60
                tvTotalStudy.text = "누적 공부 시간 : ${totalMin}분"
            })

        btnExit.setOnClickListener { finish() }
    }
}
