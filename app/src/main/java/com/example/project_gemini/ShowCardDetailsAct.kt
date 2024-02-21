package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ShowCardDetailsAct : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_card_details)

        // Get data from intent
        val logoURL = intent.getStringExtra("logoURL")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val contactNumber = intent.getStringExtra("contactNumber")

        // Initialize UI components
        val imageView = findViewById<ImageView>(R.id.imageView5)
        val textView = findViewById<TextView>(R.id.textView11)
        val textViewdesc = findViewById<TextView>(R.id.textView13)
        val cardImageView = findViewById<ImageView>(R.id.cardImage)
        val imageView8 = findViewById<ImageView>(R.id.imageView8)
        val button = findViewById<ImageButton>(R.id.button)

        // Set values to UI components
        // Use Glide to load the image into the ImageView
        Glide.with(this)
            .load(logoURL)
            .into(imageView)

        textView.text = title
        textViewdesc.text = description

        // Fetch image from Firestore
        fetchImageFromFirestore(title, cardImageView)

        // Set up button click listener
        button.setOnClickListener {
            showQuestionBottomSheet(title.toString(), contactNumber)
        }

        // Set up click listener for imageView8
        imageView8.setOnClickListener {
            val intent = Intent(this, NewCardMarketAct::class.java)
            startActivity(intent)
        }

        contactNumber?.let {
            Toast.makeText(this, "Contact Number: $it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showQuestionBottomSheet(title: String, contactNumber: String?) {
        val bottomSheetDialogFragment = QuestionBottomSheetFragment(title, contactNumber)
        bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
    }

    private fun fetchImageFromFirestore(collectionId: String?, imageView: ImageView) {
        if (collectionId != null) {
            firestore.collection("Insurance_Cards")
                .document(collectionId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val imageUrl = documentSnapshot.getString("img")
                    imageUrl?.let {
                        // Use Picasso to load the image into the ImageView
                        Picasso.get().load(it).into(imageView)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }
}
