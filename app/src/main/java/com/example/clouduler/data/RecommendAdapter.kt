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

/* RecommendItem
 * - RecommendAdapter에서 사용하는 데이터 모델
 *
 * 필드 :
 * - subject : 과목명
 * - diff    : 난이도 (별 개수)
 * - imp     : 중요도 (별 개수)
 * - examDate: 시험일
 * - score   : 추천 알고리즘이 계산한 우선순위 점수
 */
data class RecommendItem(
    val subject: String,
    val diff: Int,
    val imp: Int,
    val examDate: String,
    val score: Double
)

/* RecommendAdapter
 * - RecommendActivity에서 오늘 공부할 추천 과목 리스트를 보여주는 Adapter
 * - 추천 알고리즘이 계산한 RecommendItem을 기반
 * - 과목명, 난이도/중요도, 시험일까지 남은 기간, 우선순위 점수를 표시한다.
 *
 * UI 구성:
 *  1) 순위 번호(tvIndex)
 *  2) 과목명(tv_name)
 *  3) 난이도(tv_difficulty)
 *  4) 중요도(tv_importance)
 *  5) 하단 정보(tvScore)
 */
class RecommendAdapter(
    private val recomds: MutableList<RecommendItem> // 추천 리스트
): RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>() {

    // RecommendViewHolder 
    // 각 항목의 UI 요소
    inner class RecommendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIndex: TextView = view.findViewById(R.id.tvIndex)
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvDiff: TextView = view.findViewById(R.id.tv_difficulty)
        val tvImp: TextView = view.findViewById(R.id.tv_importance)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
    }

    // onCreateViewHolder
    // item_recommend.xml을 inflat
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend, parent, false)

        return RecommendViewHolder(view)
    }

    // onBindViewHolder
    // 추천된 항목을 ViewHolder에 반영
    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val recomd = recomds[position]

        // 순위 표시
        holder.tvIndex.text = "${position + 1}"
        holder.tvName.text = recomd.subject

        // 난이도/중요도 -> 별 개수로 표시
        holder.tvDiff.text = "난이도: ${"★".repeat(recomd.diff.toInt())}"
        holder.tvImp.text = "중요도: ${"★".repeat(recomd.imp.toInt())}"

        // 오늘 날짜/시험일
        val today = LocalDate.now()
        val examDate = LocalDate.parse(recomd.examDate)
        val daysBetween = ChronoUnit.DAYS.between(today,examDate).toInt()

        // D-Day 계산
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

        // 우선순위 점수
        holder.tvScore.text = "${recomd.examDate} | $d_Day | 우선순위 점수 ${String.format("%.1f", recomd.score)}"
    }

    // 리스트 크기 반환
    override fun getItemCount() = recomds.size

    // updateData
    // 추천 리스트를 갱신할 때
    fun updateData(recommedList: List<RecommendItem>) {
        recomds.clear()
        recomds.addAll(recommedList)
        notifyDataSetChanged()
    }
}