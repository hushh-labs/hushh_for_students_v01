package com.example.project_gemini

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QAAdapter : RecyclerView.Adapter<QAAdapter.QAViewHolder>() {

    private var qaList: List<QAItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QAViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_qa_item, parent, false)
        return QAViewHolder(view)
    }

    override fun onBindViewHolder(holder: QAViewHolder, position: Int) {
        val qaItem = qaList[position]
        holder.bind(qaItem)
    }

    override fun getItemCount(): Int {
        return qaList.size
    }

    fun submitList(newList: List<QAItem>) {
        qaList = newList
        notifyDataSetChanged()
    }

    class QAViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionTextView: TextView = itemView.findViewById(R.id.textView6)
        private val answerTextView: TextView = itemView.findViewById(R.id.textView7)

        fun bind(qaItem: QAItem) {
            questionTextView.text = qaItem.question
            answerTextView.text = qaItem.answer
        }
    }
}
