package com.example.clouduler.ui

import com.example.clouduler.ui.calendar.MultipleDotSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class EventDecorator(
    private val day: CalendarDay,
    private val colors: List<Int>
) : DayViewDecorator{

    override fun shouldDecorate(d: CalendarDay?): Boolean {
        return d == day
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(MultipleDotSpan(6f, colors))
    }
}