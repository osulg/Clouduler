package com.example.clouduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.RecommendAdapter
import com.example.clouduler.data.RecommendItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/* RecommendActivity
 * - 등록된 과목 데이터를 기반 -> 오늘의 추천 과목 생서
 * - (난이도 * 0.4 + 중요도 * 0.6) / 남은 일수
 * - 점수가 높을수록 우선적으로 학습
 * - RecyclerView로 추천결과 정렬 및 표시
*/
class RecommendActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 뒤로가기 버튼 -> MainActivity
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ----- Room DB 및 DAO ----- //
        val dao = AppDatabase.getDatabase(this@RecommendActivity).subjectDao()

        // LiveData 형태로 모든 과목 관착
        // 데이터 추가 및 수정 -> 자동 계산
        dao.getAllSubjects().observe(this, Observer { subj ->
            // 과목이 없다면 -> 리스트 생성 불가 -> 종료
            if(subj.isNullOrEmpty())
                return@Observer

            lifecycleScope.launch(Dispatchers.Default){
                val today = LocalDate.now() // 오늘 날짜

                // 오늘 이전에 끝난 시험? -> 추천 대상에서 제외
                val validSubject = subj.filter { subject ->
                    val examDate = LocalDate.parse(subject.examDate)
                    !examDate.isBefore(today) // examDate >= today
                }

                // 추천점수 계산
                val items = validSubject.map { subj ->
                    val examDate = LocalDate.parse(subj.examDate)

                    // 시험까지 남은 일수 계산
                    val daysBetween = ChronoUnit.DAYS.between(today,examDate).toDouble()

                    // 우선순우 계산
                    val priority = (subj.difficulty.toDouble() * 0.4 + subj.importance.toDouble() * 0.6)

                    // 남은일수가 1인 경우 1로 계산
                    val score = priority / (if (daysBetween <= 0) 1.0 else daysBetween)

                    // RecyclerView에서 사용할 RecommendItem으로 변환
                    RecommendItem(
                        subject = subj.name,
                        diff = subj.difficulty.toInt(),
                        imp = subj.importance.toInt(),
                        examDate = subj.examDate,
                        score = score
                    )

                }.sortedByDescending { it.score } // 점수 내림차순

                // 메인스레드에서 RecyclerView 업데이트
                withContext(Dispatchers.Main) {
                    adapter = RecommendAdapter(items.toMutableList())
                    recyclerView.adapter = adapter
                }
            }
        })
    }
}
