package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.project_gemini.databinding.ActivityNewCardMarketBinding
import com.google.firebase.firestore.FirebaseFirestore

class NewCardMarketAct : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityNewCardMarketBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCardMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contactNumber = intent.getStringExtra("CONTACT_NUMBER")

        binding.imageView8.setOnClickListener {
            val intent = Intent(this, Hushh_Home_Screen::class.java)
            startActivity(intent)
        }

        fetchAndDisplaySliderImages()

        setupRecyclerView("General preference - Personal", binding.recyclerViewHorizontal, contactNumber, true)
        setupRecyclerView("Fashion, Dress, Personal", binding.horizontalGridView, contactNumber, false)
        setupRecyclerView("Health, Life, Property : Insurance", binding.horizontalGridView2, contactNumber, false)
        setupRecyclerView("Culinary, Unwind, Leisure", binding.horizontalGridView3, contactNumber, false)
        setupRecyclerView("Travel, Roam, Explore", binding.horizontalGridView4, contactNumber, false)
        setupRecyclerView("Shopping, Hunt, Obtain", binding.horizontalGridView5, contactNumber, false)
        setupRecyclerView("Hospitality, Stay, Accommodation", binding.horizontalGridView6, contactNumber, false)


    }

    private fun setupRecyclerView(category: String, recyclerView: RecyclerView, contactNumber: String?, isHorizontal: Boolean) {
        firestore.collection("buisness_onboard")
            .whereEqualTo("Category", category)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val cardItems = ArrayList<CardItem>()
                for (document in querySnapshot) {
                    val logoURL = document.getString("logoURL") ?: ""
                    val title = document.getString("Name") ?: ""
                    val description = document.getString("Category") ?: ""
                    val rating = document.getString("Rating") ?: ""
                    val downloads = document.getString("Downloads") ?: ""

                    val cardItem = CardItem(logoURL, title, description, rating, downloads)
                    cardItems.add(cardItem)
                }

                val layoutManager = if (isHorizontal) {
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                } else {
                    GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false)
                }

                recyclerView.layoutManager = layoutManager

                val adapter = if (isHorizontal) {
                    HorizontalCardAdapter(cardItems, contactNumber.orEmpty())
                } else {
                    CardAdapter(cardItems, contactNumber.orEmpty())
                }

                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndDisplaySliderImages() {
        firestore.collection("slider_image_card_market")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val imageList = ArrayList<SlideModel>()
                for (document in querySnapshot) {
                    val imageUrl = document.getString("img")
                    imageUrl?.let {
                        val slideModel = SlideModel(it, ScaleTypes.CENTER_CROP)
                        imageList.add(slideModel)
                    }
                }

                val imageSlider = findViewById<com.denzcoskun.imageslider.ImageSlider>(R.id.image_slider)
                imageSlider.setImageList(imageList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Poor internet connection", Toast.LENGTH_SHORT).show()
            }
    }
}
