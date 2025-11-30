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
import com.example.clouduler.ui.calendar.EventDecorator
import com.example.clouduler.ui.calendar.TodayDecorator
import com.example.clouduler.ui.dot.CustomDotSpan
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/* MainActivity
 * - 달력(CalendarView)에 시험일을 표시
 * - 날짜 클릭 시 팝업으로 시험 과목 표시
 * - 주요 기능(과목 추가/추천/타이머)로 이동
*/

class MainActivity : AppCompatActivity() {

    // 달력
    private lateinit var calendarView: MaterialCalendarView

    // RoomDB 및 DAO 초기화
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val dao by lazy { db.subjectDao() }

    // 현재 표시 중인 팝업 추적용
    private var currentPopup: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 주요 버튼
        val btnAdd = findViewById<ImageButton>(R.id.btn_add)
        val btnView = findViewById<ImageButton>(R.id.btn_view)
        val btnRecommend = findViewById<ImageButton>(R.id.btn_recommend)
        val btnTimer = findViewById<ImageButton>(R.id.btn_timer)

        // 날짜/시간 관려 라이브러리(ThreeTenABP) 초기화
        AndroidThreeTen.init(this)

        // ----- 화면 이동 버튼 처리 ----- //
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

        // ----- 달력 초기화 ------ //
        calendarView = findViewById(R.id.calendarView)

        // 1) 날짜 헤더 -> yyyy년 m월
        calendarView.setTitleFormatter(
            TitleFormatter { day ->
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)
                day.date.format(formatter)
            }
        )

        // 2) 오늘 날짜 강조 Decorator
        val todayDrawable = ContextCompat.getDrawable(this, R.drawable.num_circle)!!
        calendarView.addDecorator(TodayDecorator(todayDrawable))

        // ----- RoomDB의 LiveData 관찰 ----- //
        // - DB의 변화가 있으면 달력에 dot을 표시(시험날) 및 클리시 팝업
        lifecycleScope.launch {
            dao.getAllSubjects().observe(this@MainActivity) { subjects ->
                showDots(subjects)          // 시험일 dot 업데이트
                setupPopupClick(subjects)   // 날짜 클릭 시 팝업
            }
        }
    }

    /*
     * 달력 날짜 아래에 dot 표시
     * - 과목마다 지정된 색으로 표시됨
     * - 같은 날짜에 여러 과목이 존재하면 -> 여러 점 표시
     * - Decorator 초기화 -> 오늘 Decorator 다시 등록
     */
    private fun showDots(subjects: List<SubjectEntity>) {
        // 기존 Decorator 삭제
        calendarView.removeDecorators()

        // 오늘 날짜 Decorator 다시 추가
        val todayDrawable = ContextCompat.getDrawable(this, R.drawable.num_circle)!!
        calendarView.addDecorator(TodayDecorator(todayDrawable))

        // 날짜별 과목 색상 그룹
        val grouped = subjects.groupBy { LocalDate.parse(it.examDate) }

        // 각 날짜에 과목 색상 dot 추가하기
        for ((date, list) in grouped) {
            val day = CalendarDay.from(date)
            val colors = list.map { it.color } // 해당 날짜 시험 과목의 색
            calendarView.addDecorator(EventDecorator(day, colors))
        }
    }

    /*
     * 달력 날짜 클릭 시 시험 정보 팝업 표시
     * - 선택된 날짜에 시험 존재? -> (색 dot + 과목명)
     * - 시험이 없다면? -> 등록된 시험이 없음을 표시
     * - 팝업은 1.5초 후 자동 닫힘
     */
    private fun setupPopupClick(subjects: List<SubjectEntity>) {
        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDate = date.date.toString()
            val filtered = subjects.filter { it.examDate == selectedDate }

            // 기존 팝업 닫기
            currentPopup?.dismiss()

            // 팝업 레이아웃 inflate
            val popupView = LayoutInflater.from(this)
                .inflate(R.layout.popup_subjects, null)

            val tvSubjects = popupView.findViewById<TextView>(R.id.tvSubjects)

            // 1) 선택 날짜에 시험이 존재 -> 색 dot + 과목명
            if (filtered.isNotEmpty()) {
                val sb = SpannableStringBuilder()
                sb.append("☁ 시험 과목\n\n")

                // ● 대신 " " 채우고 색 dot span 넣기
                for (subject in filtered) {
                    val line = "   ${subject.name}\n"   // ● 글자 없음
                    val start = sb.length               // 점 찍힐 위치
                    val end = start + 1

                    sb.append(line)

                    // 색 점 Span 설정
                    sb.setSpan(
                        CustomDotSpan(10f, subject.color),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                tvSubjects.text = sb
            }
            // 2) 시험이 없음
            else {
                tvSubjects.text = "❌ 등록된 시험이 없습니다."
            }

            // 3) 팝업 생성 및 위치 설정
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.contentView = popupView

            // 팝업 크기 계산 -> 달력 중앙
            popupView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )

            val popupWidth = popupView.measuredWidth
            val calendarWidth = calendarView.width

            val offsetX = (calendarWidth - popupWidth) / 2
            val offsetY = 12

            // 달력 아래 정중아에 팝업 띄우기
            popupWindow.showAsDropDown(calendarView, offsetX, offsetY)
            currentPopup = popupWindow

            // 일정 시간 후 닫기
            popupView.postDelayed({
                popupWindow.dismiss()
            }, 1500) // 1.5
        }
    }

}
