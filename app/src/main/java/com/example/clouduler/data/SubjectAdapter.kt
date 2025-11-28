package com.example.clouduler.data

import com.example.clouduler.R
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

class SubjectAdapter(
    private val subjects: MutableList<SubjectEntity>,
    private val onUpdateClick: (SubjectEntity) -> Unit,
    private val onDeleteClick: (SubjectEntity) -> Unit,
    private val onItemClick: (SubjectEntity) -> Unit
): RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    // 각 항목의 ViewHolder (아이템 하나의 모양 연결)
    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvDiff: TextView = view.findViewById(R.id.tv_difficulty)
        val tvImp: TextView = view.findViewById(R.id.tv_importance)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
        val btnUpdate: ImageButton = view.findViewById(R.id.btn_update)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)

        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]

        holder.tvName.text = "${subject.name}"
        holder.tvDate.text = "시험일: ${subject.examDate}"
        holder.tvDiff.text = "난이도: ${"★".repeat(subject.difficulty.toInt())}"
        holder.tvImp.text = "중요도: ${"★".repeat(subject.importance.toInt())}"

        holder.btnDelete.setOnClickListener {
            onDeleteClick(subject)
        }
        holder.btnUpdate.setOnClickListener {
            onUpdateClick(subject)
        }
        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }
    }

    override fun getItemCount() = subjects.size

    fun updateData(newSubjects: List<SubjectEntity>) {
        subjects.clear()
        subjects.addAll(newSubjects)
        notifyDataSetChanged()
    }
}