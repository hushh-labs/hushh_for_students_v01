package com.example.project_gemini

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_gemini.R

class HistoryCoinsAdapter(private val coinList: List<HistoryCoins>) :
    RecyclerView.Adapter<HistoryCoinsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fieldName: TextView = itemView.findViewById(R.id.textView18)
        val fieldValue: TextView = itemView.findViewById(R.id.textView17)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coinItem = coinList[position]
        holder.fieldName.text = coinItem.fieldName
        holder.fieldValue.text = coinItem.fieldValue
    }

    override fun getItemCount(): Int {
        return coinList.size
    }
}
