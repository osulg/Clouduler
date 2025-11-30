package com.example.clouduler.ui.calendar

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/* TodayDecorator
 * - 달력에서 오늘 날짜를 꾸미는 Decorator
 * - MaterialCalendarView의 DayViewDecorator 적용 방식 사용
 *
 * 기능 :
 * - shouldDecorator() : 지금 날짜와 비교
 * - decorate() : 해당 날짜의 배경을 지정된 drawable로 변경 + 날짜색 변경(흰색)
 * 
 * 위치 : 
 * - MainActivity -> onCreate()에서 달력 초기화
 * 
 * 결과 :
 * - 오늘 날짜 칸이 동그랗게 강조됨
*/
class TodayDecorator(private val drawable: Drawable) : DayViewDecorator{
    private val today = CalendarDay.today() // 오늘 날짜

    // 오늘 날짜일 때에만 decorator 적용됨
    override fun shouldDecorate(day:CalendarDay): Boolean {
        return day == today
    }

    // 실제 decoration : 배경 + 글자색 변경
    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
        view.addSpan(android.text.style.ForegroundColorSpan(Color.WHITE))
    }


}