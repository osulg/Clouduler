package com.example.clouduler.ui.calendar

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class CustomDotSpan(
    private val radius: Float,
    private val color: Int
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // 점 + 약간의 여백
        return (radius * 3).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val oldColor = paint.color
        paint.color = color

        // 텍스트 높이 중앙에 점을 그린다
        val centerY = (top + bottom) / 2f
        canvas.drawCircle(x + radius, centerY, radius, paint)

        paint.color = oldColor
    }
}