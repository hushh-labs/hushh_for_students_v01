package com.example.project_gemini

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jackandphantom.carouselrecyclerview.view.ReflectionImageView

class CardRecyclerAdapter(
    private val cardList: List<CustomCardItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CardRecyclerAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCard: ReflectionImageView = itemView.findViewById(R.id.imageViewCardLoader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_card_homecards_item, parent, false)

        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentItem = cardList[position]

        // Load image into ReflectionImageView using a library like Glide or Picasso
        Glide.with(holder.imageViewCard.context).load(currentItem.imageUrl)
            .into(holder.imageViewCard)

        holder.itemView.setOnClickListener {
            // Pass the parent node name to the click listener
            onItemClick(currentItem.parentName)
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }
}
