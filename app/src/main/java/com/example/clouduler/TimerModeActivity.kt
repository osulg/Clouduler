package com.example.clouduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class TimerModeActivity : AppCompatActivity() {
    private var selectedSubjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode_timer)

        val btnPomodoro = findViewById<ImageButton>(R.id.btnPomodoro)
        val btnNormal = findViewById<ImageButton>(R.id.btnNormal)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnTimerSubject = findViewById<ImageButton>(R.id.btnTimerSubject)

        btnPomodoro.setOnClickListener {
            val intent = Intent(this, PomodoroTimerActivity::class.java)
            intent.putExtra("subjectID", selectedSubjectId)
            startActivity(intent)
        }

        btnNormal.setOnClickListener {
            val intent = Intent(this, NormalTimerActivity::class.java)
            intent.putExtra("subjectID", selectedSubjectId)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnTimerSubject.setOnClickListener {
            val bottomSheet = SubjectSelectBottomSheet { subjectId ->
                selectedSubjectId = subjectId

                if (subjectId != null) {
                    btnTimerSubject.setImageResource(R.drawable.btn_timer_subject_selected)
                } else {
                    btnTimerSubject.setImageResource(R.drawable.btn_timer_subject)
                }
            }

            bottomSheet.show(supportFragmentManager, "subject_select")
        }
    }
}