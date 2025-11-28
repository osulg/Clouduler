package com.example.clouduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.R

class SubjectSelectAdapter(
    private val subjects: List<SubjectEntity>,
    private val onItemClick: (Int?) -> Unit
) : RecyclerView.Adapter<SubjectSelectAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvSubjectName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_select, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = subjects.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjects[position]
        holder.name.text = subject.name

        holder.itemView.setOnClickListener {
            onItemClick(subject.id)   // subjectId 반환
        }
    }
}
