package com.example.clouduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class RecommendItem(
    val subject: String,
    val diff: Int,
    val imp: Int,
    val examDate: String,
    val score: Double
)

class RecommendAdapter(
    private val recomds: MutableList<RecommendItem>
): RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>() {

    // 각 항목의 ViewHolder (아이템 하나의 모양 연결)
    inner class RecommendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIndex: TextView = view.findViewById(R.id.tvIndex)
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvDiff: TextView = view.findViewById(R.id.tv_difficulty)
        val tvImp: TextView = view.findViewById(R.id.tv_importance)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend, parent, false)

        return RecommendViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val recomd = recomds[position]

        holder.tvIndex.text = "${position + 1}"
        holder.tvName.text = recomd.subject
        holder.tvDiff.text = "난이도: ${"★".repeat(recomd.diff.toInt())}"
        holder.tvImp.text = "중요도: ${"★".repeat(recomd.imp.toInt())}"

        val today = LocalDate.now()
        val examDate = LocalDate.parse(recomd.examDate)
        val daysBetween = ChronoUnit.DAYS.between(today,examDate).toInt()

        val d_Day: String =
            if(daysBetween>0){
            "D-${daysBetween}"
            }
            else if(daysBetween==0){
                "D-Day"
            }
            else{
                "D+${-daysBetween}"
            }

        holder.tvScore.text = "${recomd.examDate} | $d_Day | 우선순위 점수 ${String.format("%.1f", recomd.score)}"
    }

    override fun getItemCount() = recomds.size

    fun updateData(recommedList: List<RecommendItem>) {
        recomds.clear()
        recomds.addAll(recommedList)
        notifyDataSetChanged()
    }
}