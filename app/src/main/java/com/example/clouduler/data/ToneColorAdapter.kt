package com.example.clouduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.R

class ToneColorAdapter(
    private val tones : List<Int>,
    private val onClick: (Int)->Unit
)  : RecyclerView.Adapter<ToneColorAdapter.ToneViewHolder>() {

    inner class ToneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val circle = itemView.findViewById<View>(R.id.colorCircle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToneViewHolder {
        val view = LayoutInflater.from((parent.context))
            .inflate(R.layout.color_item_circle,parent,false)

        return ToneViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToneViewHolder, position: Int) {
        val color = tones[position]

        holder.circle.setBackgroundColor(color)

        holder.itemView.setOnClickListener{
            onClick(color)
        }
    }

    override fun getItemCount(): Int = tones.size
}