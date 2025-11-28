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

class RecommendActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val dao = AppDatabase.getDatabase(this@RecommendActivity).subjectDao()

        dao.getAllSubjects().observe(this, Observer { subj ->
            if(subj.isNullOrEmpty())
                return@Observer

            lifecycleScope.launch(Dispatchers.Default){
                val today = LocalDate.now()

                val items = subj.map { subj ->
                    val examDate = LocalDate.parse(subj.examDate)
                    val daysBetween = ChronoUnit.DAYS.between(today,examDate).toDouble()
                    val priority = (subj.difficulty.toDouble() * 0.4 + subj.importance.toDouble() * 0.6)
                    val score = priority / (if (daysBetween <= 0) 1.0 else daysBetween)

                    RecommendItem(
                        subject = subj.name,
                        diff = subj.difficulty.toInt(),
                        imp = subj.importance.toInt(),
                        examDate = subj.examDate,
                        score = score
                    )

                }.sortedByDescending { it.score }

                withContext(Dispatchers.Main) {
                    adapter = RecommendAdapter(items.toMutableList())
                    recyclerView.adapter = adapter
                }
            }
        })
    }
}
