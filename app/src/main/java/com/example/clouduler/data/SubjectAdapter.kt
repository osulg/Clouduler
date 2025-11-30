package com.example.clouduler.data

import com.example.clouduler.R
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

/* SubjectAdapter
 * 기능:
 * 1) 과목명, 시험일, 난이도/중요도(별) 표시
 * 2) 과목 수정 버튼 클릭 -> onUpdateClick(subject)
 * 3) 과목 삭제 버튼 클릭 -> onDeleteClick(subject)
 * 4) 리스트 항목 클릭 -> onItemClick(subject)
 */
class SubjectAdapter(
    private val subjects: MutableList<SubjectEntity>, // 표시할 과목 리스트
    private val onUpdateClick: (SubjectEntity) -> Unit, // 수정
    private val onDeleteClick: (SubjectEntity) -> Unit, // 삭제
    private val onItemClick: (SubjectEntity) -> Unit // 항목 클릭
): RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    // SubjectViewHolder
    // - 단일 과목의 View 요소의 구성요소를 결정
    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvDiff: TextView = view.findViewById(R.id.tv_difficulty)
        val tvImp: TextView = view.findViewById(R.id.tv_importance)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
        val btnUpdate: ImageButton = view.findViewById(R.id.btn_update)
    }

    // onCreateViewHolder
    // - item_subject.xml 레이아웃을 inlfate
    // - ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)

        return SubjectViewHolder(view)
    }

    // onBindViewHolder
    // - position에 해당하는 SubjectEntitiy 데이터를 ViewHolder에 매핑
    // - 클릭에 대한 이벤트 연결
    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]

        // 과목명, 시험일, 난이도/중요도
        holder.tvName.text = "${subject.name}"
        holder.tvDate.text = "시험일: ${subject.examDate}"
        holder.tvDiff.text = "난이도: ${"★".repeat(subject.difficulty.toInt())}"
        holder.tvImp.text = "중요도: ${"★".repeat(subject.importance.toInt())}"

        // 삭제 버튼 클릭
        holder.btnDelete.setOnClickListener {
            onDeleteClick(subject)
        }
        // 수정 버튼 클릭
        holder.btnUpdate.setOnClickListener {
            onUpdateClick(subject)
        }
        // 항목 클릭
        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }
    }

    // 리스트 크기 반환
    override fun getItemCount() = subjects.size

    // updateData
    // - 과목 리스트를 ViewActivity에서 갱신할 때 호출
    fun updateData(newSubjects: List<SubjectEntity>) {
        subjects.clear()
        subjects.addAll(newSubjects)
        notifyDataSetChanged() // 전체 RecyclerView 갱신
    }
}