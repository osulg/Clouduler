package com.example.clouduler.data

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.R

/* ParentColorAdapter
 * - 색상 선택 Adapter
 *
 * 위치:
 *  - AddActivity -> 과목 생성/수정 시 색상 선택 BottomSheet
 *
 * 역할:
 *  - 제공된 colors 리스트를 RecyclerView의 circle 형태로 표시
 *  - 각 색상(circle) 클릭 시 onClick(color)를 통해 선택된 색값 전달
 */
class ParentColorAdapter(
    private val colors : List<Int>, // 색상 리스트
    private val onClick: (Int)->Unit // 클릭시 호출
)  : RecyclerView.Adapter<ParentColorAdapter.ColorViewHolder>() {

    // ColorViewHolder
    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val circle = itemView.findViewById<View>(R.id.colorCircle)
    }

    // onCreateViewHolder
    // color_item_circle.xml을 inflate하여 ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from((parent.context))
            .inflate(R.layout.color_item_circle,parent,false)

        return ColorViewHolder(view)
    }

    // onBindViewHolder
    // circle 배경에 색상 적용
    // 클릭하면 색을 onClick으로 전달
    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]

        val drawable = holder.circle.background.mutate() as GradientDrawable
        drawable.setColor(color)

        holder.itemView.setOnClickListener {
            onClick(color)
        }
    }

    // 리스트 크기 전달
    override fun getItemCount(): Int = colors.size
}