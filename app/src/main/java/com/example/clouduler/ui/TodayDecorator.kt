package com.example.clouduler.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TodayDecorator(private val drawable: Drawable) : DayViewDecorator{
    private val today = CalendarDay.today()

    override fun shouldDecorate(day:CalendarDay): Boolean {
        return day == today
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
        view.addSpan(android.text.style.ForegroundColorSpan(Color.WHITE))
    }


}