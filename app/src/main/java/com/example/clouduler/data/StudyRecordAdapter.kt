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

        val percent =
            if(maxMinutes>0){
                (minutes*100/maxMinutes)
            }
            else{
                0
            }

        val barBack = holder.itemView.findViewById<View>(R.id.barBack)
        val barFill = holder.itemView.findViewById<View>(R.id.barFill)

        barBack.post {
            val maxWidth = barBack.width
            val newWidth = (maxWidth * percent / 100)
            val params = barFill.layoutParams
            params.width = newWidth
            barFill.layoutParams = params

            DrawableCompat.setTint(barFill.background, subjectColor)

        }
    }

    override fun getItemCount(): Int = records.size

    fun updateData(newRecords: List<DailyRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
