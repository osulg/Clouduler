package com.example.clouduler.data

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.R

class ParentColorAdapter(
    private val colors : List<Int>,
    private val onClick: (Int)->Unit
)  : RecyclerView.Adapter<ParentColorAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val circle = itemView.findViewById<View>(R.id.colorCircle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from((parent.context))
            .inflate(R.layout.color_item_circle,parent,false)

        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]

        val drawable = holder.circle.background.mutate() as GradientDrawable
        drawable.setColor(color)

        holder.itemView.setOnClickListener {
            onClick(color)
        }
    }

    override fun getItemCount(): Int = colors.size
}