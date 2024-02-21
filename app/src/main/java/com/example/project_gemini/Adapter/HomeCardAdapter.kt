package com.example.project_gemini.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_gemini.R

class HomeCardAdapter(private var imageURLs: List<String>) : RecyclerView.Adapter<HomeCardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCardLoader: ImageView = itemView.findViewById(R.id.imageViewCardLoader)
    }

    fun setImageURLs(newList: List<String>) {
        imageURLs = newList
        notifyDataSetChanged()
    }

    fun getImageURLs(): List<String> {
        return imageURLs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_card_homecards_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Load image using Glide or your preferred image loading library
        Glide.with(holder.itemView)
            .load(imageURLs[position])
            .into(holder.imageViewCardLoader)
    }

    override fun getItemCount(): Int {
        return imageURLs.size
    }
}
