package com.example.clouduler.ui.calendar

import com.example.clouduler.ui.dot.MultipleDotSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/* EventDecorator
 * - 특정 날짜에 색상 dot을 표시하는 데코레이터
 * - 시험일에 표시하는 용도
 *
 * 기능 :
 * - shouldDecorator() : 현재 셀의 날짜 d가 지정된 day와 동일하다면 true
 * - decorate() : 선택된 색상을 기반으로 MultiDotSpan 추가
 *
 * MultiDotSpan :
 * - 1개의 날짜에 여러 과목 있으면? -> 색상 dot을 여러 개 표시
 *
 * 위치 :
 * - MainActivity -> showDots()
 *
 * 결과 :
 * - 특정 날짜 아래에 여러 개의 색상 dot이 생김 -> 시험 날짜 표시
 * - 과목이 여러개면? -> 여러개 표시
*/
class EventDecorator(
    private val day: CalendarDay, // decorate를 적용할 날짜
    private val colors: List<Int> // 표시할 dot 색상 목록
) : DayViewDecorator{

    // 지정한 날짜(day)에만 decorate 실행
    override fun shouldDecorate(d: CalendarDay?): Boolean {
        return d == day
    }

    // MultiDotSpan 적용
    // dot 크기 -> 6f
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(MultipleDotSpan(6f, colors))
    }
}