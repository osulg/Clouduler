package com.example.clouduler.ui.calendar

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import androidx.annotation.ColorInt
import kotlin.math.min

class MultipleDotSpan(
    private val radius: Float,
    @ColorInt private val colors: List<Int>
) : LineBackgroundSpan {
    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        if(colors.isEmpty()) return

        val total = min(colors.size,4) // 최대 5개
        
        // 점 크기 및 위치
        val width = right - left
        val centerX = left + width / 2f
        val centerY = bottom + radius * 2.8f

        //
        val perRow = 2
        val rows = (total+1)/2

        val gapX = radius * 2.6f
        val gapY = radius * 2.8f

        val oldColor = paint.color
        val oldStyle = paint.style
        paint.style = Paint.Style.FILL

        var index = 0
        for (row in 0 until rows) {
            val dotsInRow = min(perRow, total - index)
            // 각 행의 가로 중심 정렬
            val totalWidth = (dotsInRow - 1) * gapX
            var startX = centerX - totalWidth / 2f
            val y = centerY - (rows - 1 - row) * gapY  // 위→아래 순으로 표시

            for (i in 0 until dotsInRow) {
                paint.color = colors[index]
                canvas.drawCircle(startX, y, radius, paint)
                startX += gapX
                index++
            }
        }
        paint.color = oldColor
        paint.style = oldStyle
        
    }
}