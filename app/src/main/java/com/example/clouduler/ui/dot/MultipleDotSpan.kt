package com.example.clouduler.ui.dot

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import androidx.annotation.ColorInt
import kotlin.math.min

/* MultipleDotSpan
 * - MaterialCalendarView 날짜 아래에 여러 개의 dot을 그리는 Span
 * - 특정 날짜에 여러 과목(여러 색상)이 있을 경우 사용됨
 *
 * 위치:
 * - EventDecorator → decorate():
 *
 * 특징:
 * - LineBackgroundSpan: 텍스트 라인의 배경 영역에 직접 그리는 Span
 * - colors 리스트의 색상 수만큼 점을 찍음 (최대 4~5개까지)
 * - 날짜 아래 정렬
 */
class MultipleDotSpan(
    private val radius: Float, // 반지름
    @ColorInt private val colors: List<Int> // dot 색상 목록
) : LineBackgroundSpan {

    // drawBackground()
    // - 날짜 영역에 여러 개의 점 배치
    // - 점은 row로 배치 -> 최대 2개
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
        // 색상 없으면 -> 안 그리기
        if(colors.isEmpty()) return

        // 날짜에 표시할 점 개수
        val total = min(colors.size,4) // 최대 4
        
        // 점 크기 및 위치
        val width = right - left
        val centerX = left + width / 2f // 날짜 가로 중앙
        val centerY = bottom + radius * 2.8f // 날짜 텍스트 아래

        // 점 배치 구조 => 2개씩 2줄
        val perRow = 2
        val rows = (total+1)/2

        // 점 사이 간격
        val gapX = radius * 2.6f // 점 가로 간격
        val gapY = radius * 2.8f // 점 세로 간격

        // 원래 상태 복구 위한 color/style 저장
        val oldColor = paint.color
        val oldStyle = paint.style
        
        // 원을 채워 그리기 -> FILL
        paint.style = Paint.Style.FILL

        var index = 0
        for (row in 0 until rows) {
            // 해당 row에 표시할 점 개수 계산
            val dotsInRow = min(perRow, total - index)
            // 중앙 정렬 계산
            val totalWidth = (dotsInRow - 1) * gapX
            var startX = centerX - totalWidth / 2f

            // 위→아래 순으로 표시
            val y = centerY - (rows - 1 - row) * gapY

            // 현재 row 안에서 점을 순서대로 그리기
            for (i in 0 until dotsInRow) {
                paint.color = colors[index]
                canvas.drawCircle(startX, y, radius, paint)
                startX += gapX
                index++
            }
        }
        
        // 그리기 후 -> paint 상태 복구
        paint.color = oldColor
        paint.style = oldStyle
        
    }
}