package com.example.clouduler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.SubjectEntity
import com.example.clouduler.ui.EventDecorator
import com.example.clouduler.ui.TodayDecorator
import com.example.clouduler.ui.calendar.CustomDotSpan
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val dao by lazy { db.subjectDao() }
    private var currentPopup: PopupWindow? = null   // 현재 표시 중인 팝업 추적용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAdd = findViewById<ImageButton>(R.id.btn_add)
        val btnView = findViewById<ImageButton>(R.id.btn_view)
        val btnRecommend = findViewById<ImageButton>(R.id.btn_recommend)
        val btnTimer = findViewById<ImageButton>(R.id.btn_timer)

        // ThreeTenABP 초기화
        AndroidThreeTen.init(this)

        // 버튼 클릭 이동
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }
        btnView.setOnClickListener {
            startActivity(Intent(this, ViewActivity::class.java))
        }
        btnRecommend.setOnClickListener {
            startActivity(Intent(this, RecommendActivity::class.java))
        }
        btnTimer.setOnClickListener {
            startActivity(Intent(this, TimerModeActivity::class.java))
        }

        // 달력 초기화
        calendarView = findViewById(R.id.calendarView)

        // 날짜 헤더
        calendarView.setTitleFormatter(
            TitleFormatter { day ->
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)
                day.date.format(formatter)
            }
        )

        // 오늘 날짜 Decorator
        val todayDrawable = ContextCompat.getDrawable(this, R.drawable.num_circle)!!
        calendarView.addDecorator(TodayDecorator(todayDrawable))

        // DB 관찰
        lifecycleScope.launch {
            dao.getAllSubjects().observe(this@MainActivity) { subjects ->
                showDots(subjects)
                setupPopupClick(subjects)
            }
        }
    }

    private fun showDots(subjects: List<SubjectEntity>) {
        calendarView.removeDecorators()

        // Decorator 지운 후 오늘 데코레이터 다시 추가
        val todayDrawable = ContextCompat.getDrawable(this, R.drawable.num_circle)!!
        calendarView.addDecorator(TodayDecorator(todayDrawable))

        // 날짜별 과목 색상 매핑
        val grouped = subjects.groupBy { LocalDate.parse(it.examDate) }

        for ((date, list) in grouped) {
            val day = CalendarDay.from(date)
            val colors = list.map { it.color }
            calendarView.addDecorator(EventDecorator(day, colors))
        }
    }

    // 날짜 클릭 시 팝업 표시
    private fun setupPopupClick(subjects: List<SubjectEntity>) {
        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDate = date.date.toString()
            val filtered = subjects.filter { it.examDate == selectedDate }

            // 기존 팝업 닫기
            currentPopup?.dismiss()

            // 팝업 inflate
            val popupView = LayoutInflater.from(this)
                .inflate(R.layout.popup_subjects, null)

            val tvSubjects = popupView.findViewById<TextView>(R.id.tvSubjects)

            // ------------------------------
            // ① 시험이 있는 경우 → 색 dot + 과목명 표시
            // ------------------------------
            if (filtered.isNotEmpty()) {

                val sb = SpannableStringBuilder()
                sb.append("☁ 시험 과목\n\n")

                for (subject in filtered) {

                    val line = "   ${subject.name}\n"  // ● 글자 없음
                    val start = sb.length               // 점 찍힐 위치
                    val end = start + 1

                    sb.append(line)

                    sb.setSpan(
                        CustomDotSpan(10f, subject.color),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                tvSubjects.text = sb
            }
            // ------------------------------
            // ② 시험이 없는 경우
            // ------------------------------
            else {
                tvSubjects.text = "❌ 등록된 시험이 없습니다."
            }

            // ------------------------------
            // 팝업 생성
            // ------------------------------
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.contentView = popupView

            // 팝업 크기 측정
            popupView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )

            val popupWidth = popupView.measuredWidth
            val calendarWidth = calendarView.width

            val offsetX = (calendarWidth - popupWidth) / 2
            val offsetY = 12

            popupWindow.showAsDropDown(calendarView, offsetX, offsetY)
            currentPopup = popupWindow

            // 자동 닫기
            popupView.postDelayed({
                popupWindow.dismiss()
            }, 1500)
        }
    }

}
