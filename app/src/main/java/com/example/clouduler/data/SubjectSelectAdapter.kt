package com.example.clouduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.R

/* SubjectSelectAdapter
 *  - SubjectSelectBottomSheet 안에서 과목 목록을 표시하는 Adapter
 *
 * 위치:
 *  - SubjectSelectBottomSheet -> 과목 선택 BottomSheet
 *  - TimerModeActivity -> 과목 선택 버튼 눌렀을 때 BottomSheet 표시
 */
class SubjectSelectAdapter(
    private val subjects: List<SubjectEntity>, // 표시할 과목 리스트
    private val onItemClick: (Int?) -> Unit // 리스트 클릭
) : RecyclerView.Adapter<SubjectSelectAdapter.ViewHolder>() {

    // ViewHolder
    // item_subject_select.xml 내부와 View를 연결
    // 과목명 표시
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvSubjectName)
    }

    // onCreateViewHolder
    // item 레이아웃을 inflate하여 ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_select, parent, false)
        return ViewHolder(v)
    }

    // 리스트 크기 반환
    override fun getItemCount() = subjects.size

    // onBindViewHodler
    // 각 항목 선택
    // 선택한 과목 ID를 전달
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjects[position]
        // 과목명 표시
        holder.name.text = subject.name

        holder.itemView.setOnClickListener {
            onItemClick(subject.id)   // subjectId 반환
        }
    }
}
