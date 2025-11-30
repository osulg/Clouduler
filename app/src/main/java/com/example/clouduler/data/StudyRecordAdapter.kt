package com.example.clouduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.DailyRecord
import com.example.clouduler.R

/* StudyRecordAdapter
 * - SubjectDetailActivity에서 날짜별 공부 기록 리스트"를 표시
 *
 *
 * UI 구조 :
 * 1) 날짜 텍스트
 * 2) 공부 시간 텍스트
 * 3) bar chart
 * - barBack : 회색 배경 바 (전체 길이 기준)
 * - barFill : 실제 공부시간에 비례해서 채워지는 컬러 바
 */
class StudyRecordAdapter(
    private var records: List<DailyRecord>
) : RecyclerView.Adapter<StudyRecordAdapter.RecordViewHolder>() {

    // 과목 색상
    // SubjectDetailActivity에서 전달 받음
    private var subjectColor: Int = 0

    // setSubjectColor
    // - barFill의 색을 바꿀 때 사용
    fun setSubjectColor(color: Int) {
        subjectColor = color
        notifyDataSetChanged()
    }

    // RecordViewHolder
    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecordDate: TextView = itemView.findViewById(R.id.tvRecordDate)
        val tvRecordTime: TextView = itemView.findViewById(R.id.tvRecordTime)
    }

    // onCreateViewHolder
    // xml 레이아웃 inflate -> ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_record, parent, false)
        return RecordViewHolder(view)
    }

    // onBindViewHolder
    // - 날짜별 공부시간을 ViewHolder에 바인딩
    // - barFill 너비를 해당 날짜 공부 시간 비율에 따라 반영
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        // ms -> 분
        val minutes = (record.totalTime / 1000L / 60L).toInt()

        holder.tvRecordDate.text = record.date
        holder.tvRecordTime.text = "${minutes}분"

        // 막대그래프 비율 계산
        // 가장 많이 공부한 날을 max로 설정
        val maxMinutes = records
            .maxOfOrNull { (it.totalTime / 1000L / 60L).toInt() }
            ?: 1

        // 비율 계산 -> 0~100
        val percent =
            if(maxMinutes>0){
                (minutes*100/maxMinutes)
            }
            else{
                0
            }

        // barBack(전체) / barFill (채워짐)
        val barBack = holder.itemView.findViewById<View>(R.id.barBack)
        val barFill = holder.itemView.findViewById<View>(R.id.barFill)

        // barBack의 width는 화면에 실제로 배치된 뒤에 결정
        barBack.post {
            val maxWidth = barBack.width // 전체 막대 너비
            val newWidth = (maxWidth * percent / 100) // 비율에 따른 barFill 너비
            val params = barFill.layoutParams
            params.width = newWidth
            barFill.layoutParams = params

            // barFill 색상 적용
            DrawableCompat.setTint(barFill.background, subjectColor)

        }
    }
    
    // 리스트 크기 반환
    override fun getItemCount(): Int = records.size

    // updateData
    // -LiveData로 관찰된 최산 DailyRecord 리스트 전달
    fun updateData(newRecords: List<DailyRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
