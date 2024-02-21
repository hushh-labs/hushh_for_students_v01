package com.example.project_gemini

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_gemini.databinding.LayoutCardBrandItemBinding
import com.google.firebase.firestore.FirebaseFirestore

class HorizontalCardAdapter(private val cardItems: List<CardItem>, private val contactNumber: String) :
    RecyclerView.Adapter<HorizontalCardAdapter.HorizontalCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalCardViewHolder {
        val binding =
            LayoutCardBrandItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HorizontalCardViewHolder(binding, contactNumber)
    }

    override fun onBindViewHolder(holder: HorizontalCardViewHolder, position: Int) {
        val cardItem = cardItems[position]
        holder.bind(cardItem)
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }

    class HorizontalCardViewHolder(private val binding: LayoutCardBrandItemBinding, private val contactNumber: String) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cardItem: CardItem) {
            binding.apply {
                // Load image using Glide
                Glide.with(itemView.context)
                    .load(cardItem.logoURL)
                    .into(imageView12)

                textView2.text = cardItem.title
                textView3.text = cardItem.description
                textView4.text = cardItem.rating
                textView5.text = "${cardItem.downloads} Downloads"

                button.setOnClickListener {
                    // Check if the user already has the card in their wallet
                    checkCardInWallet(cardItem)
                }
                // Set additional views as needed
            }
        }

        private fun checkCardInWallet(cardItem: CardItem) {
            val cardTitle = cardItem.title
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("users")
                .document(contactNumber)
                .collection(cardTitle) // Check if subcollection exists with the card title
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // User doesn't have this card, navigate to ShowCardDetailsAct
                        navigateToShowCardDetails(cardItem)
                    } else {
                        // User already has this card, show a toast
                        Toast.makeText(
                            itemView.context,
                            "You already have the card '$cardTitle' in your wallet.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(
                        itemView.context,
                        "Failed to check for existing card: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        private fun navigateToShowCardDetails(cardItem: CardItem) {
            // Navigate to ShowCardDetailsAct
            val intent = Intent(itemView.context, ShowCardDetailsAct::class.java).apply {
                putExtra("logoURL", cardItem.logoURL)
                putExtra("title", cardItem.title)
                putExtra("description", cardItem.description)
                putExtra("contactNumber", contactNumber)
            }
            itemView.context.startActivity(intent)
        }
    }
}
