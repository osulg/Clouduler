package com.example.clouduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

/* TimerModeActivity
 * - 타이머 실행 전, 사용자가 어떤 모드 사용할지 결정
 *
 * 기능 :
 * - 뽀모도로 타이머
 * - 일반 타이머 실행
 * - 타이머에 기록을 저장할 과목 선택 (필수 X, 선택 기능)
 *
 * 동작 :
 * - 과목 선택 버튼 -> BottomSheet로 Subject 리스트 표시 -> 선택
 * - 선택된 과목 ID를 selectedSubjectId에 저장
 * - PomodoroTimerActivity, NormalTimerActivity 실행 시 함께 전달
 */
class TimerModeActivity : AppCompatActivity() {
    // 사용자가 선택한 과목의 ID
    // 있을수도 없을수도 있음
    private var selectedSubjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode_timer)

        // UI 요소
        val btnPomodoro = findViewById<ImageButton>(R.id.btnPomodoro)
        val btnNormal = findViewById<ImageButton>(R.id.btnNormal)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnTimerSubject = findViewById<ImageButton>(R.id.btnTimerSubject)

        // ----- 뽀모도로 모드 이동 ------ //
        btnPomodoro.setOnClickListener {
            val intent = Intent(this, PomodoroTimerActivity::class.java)
            intent.putExtra("subjectID", selectedSubjectId)
            startActivity(intent)
        }

        // ------ 일반 모드 이동 ------ //
        btnNormal.setOnClickListener {
            val intent = Intent(this, NormalTimerActivity::class.java)
            intent.putExtra("subjectID", selectedSubjectId)
            startActivity(intent)
        }

        // ------ 뒤로가기 -> 메인화면 ------ //
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ------ 타이머 기록을 연결할 과목 선택 ------ //
        btnTimerSubject.setOnClickListener {
            // BottomSheet 생성
            val bottomSheet = SubjectSelectBottomSheet { subjectId ->
                // 선택한 과목 ID 저장
                selectedSubjectId = subjectId

                // 선택 여부에 따라 버튼 이미지 변경됨.
                if (subjectId != null) {
                    btnTimerSubject.setImageResource(R.drawable.btn_timer_subject_selected)
                } else {
                    btnTimerSubject.setImageResource(R.drawable.btn_timer_subject)
                }
            }

            // BottomSheet 표시
            bottomSheet.show(supportFragmentManager, "subject_select")
        }
    }
}