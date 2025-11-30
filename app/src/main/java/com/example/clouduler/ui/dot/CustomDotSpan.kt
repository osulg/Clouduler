package com.example.clouduler.ui.dot

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

/* CustomDotSpan
 * - TextView 안에서 텍스트 앞에 dot을 그리는 Span
 * - 날짜 클릭 시 뜨는 팝업에서 과목명 앞에 색 표시
 *
 * 특징 :
 * - ReplacementSpan을 상속 -> 텍스트 대신 원하는 그래픽 그림
 * - CustomDotSpan -> 1개의 점 그리는 Span
 *
 * MultiDotSpan :
 * - 1개의 날짜에 여러 과목 있으면? -> 색상 dot을 여러 개 표시
 *
 * 위치 :
 * - MainActivity -> setupPopupCLick() ~
*/
class CustomDotSpan(
    private val radius: Float, // 점의 반지름
    private val color: Int // 점의 색상
) : ReplacementSpan() {

    // getSize()
    // - Span이 차지하는 가로 너비 반환
    // - 점을 그릴 공간 + 여백
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

    // draw()
    // - 점을 직접 그리는 부분
    // - 기준 좌표 기반 -> 텍스트 중아에 그림
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

        // 텍스트 높이 중앙에 점을 그림
        val centerY = (top + bottom) / 2f
        // 왼쪽으로 조금 띄워 표시
        canvas.drawCircle(x + radius, centerY, radius, paint)

        paint.color = oldColor
    }
}