package com.example.clouduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.DailyRecord
import com.example.clouduler.R

class StudyRecordAdapter(
    private var records: List<DailyRecord>
) : RecyclerView.Adapter<StudyRecordAdapter.RecordViewHolder>() {

    private var subjectColor: Int = 0

    fun setSubjectColor(color: Int) {
        subjectColor = color
        notifyDataSetChanged()
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecordDate: TextView = itemView.findViewById(R.id.tvRecordDate)
        val tvRecordTime: TextView = itemView.findViewById(R.id.tvRecordTime)
        val progressStudy: ProgressBar = itemView.findViewById(R.id.progressStudy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        val minutes = (record.totalTime / 1000L / 60L).toInt()

        holder.tvRecordDate.text = record.date
        holder.tvRecordTime.text = "${minutes}분"

        val maxMinutes = records
            .maxOfOrNull { (it.totalTime / 1000L / 60L).toInt() }
            ?: 1

        val percent = when {
            minutes == 0 -> 0
            maxMinutes == 0 -> 0
            else -> {
                // 최소 20% ~ 최대 100%
                20 + (minutes * 80 / maxMinutes)
            }
        }

        // 0분이면 progress 숨김
        if (minutes == 0) {
            holder.progressStudy.visibility = View.INVISIBLE
        } else {
            holder.progressStudy.visibility = View.VISIBLE
            holder.progressStudy.progress = percent
        }

        // 과목 색상 적용
        holder.progressStudy.progressDrawable.setTint(subjectColor)
    }



    override fun getItemCount(): Int = records.size

    fun updateData(newRecords: List<DailyRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
