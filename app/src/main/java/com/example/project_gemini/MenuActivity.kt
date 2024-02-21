package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_gemini.databinding.ActivityMenuBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserDocument: DocumentReference
    private lateinit var menuAdapter: HushhMenuAdapter
    private lateinit var CardName: String
    private lateinit var menuItems: MutableList<HushhMenuItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Assuming "contact" is the document ID
        val contact = intent.getStringExtra("contact")
        CardName = intent.getStringExtra("parentName").toString() // Save CardName
        val name = intent.getStringExtra("name")

        binding.textView11.text = name

        currentUserDocument = firestore.collection("users").document(contact.toString())

        // Setup RecyclerView
        binding.recyclerViewmenu.layoutManager = LinearLayoutManager(this)
        menuAdapter = HushhMenuAdapter(currentUserDocument, CardName) // Pass CardName here
        binding.recyclerViewmenu.adapter = menuAdapter

        // Fetch menu items from Firestore
        fetchMenuItems()

        binding.searchForACard.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim().toLowerCase(Locale.getDefault())
                val filteredList = if (searchText.isNotEmpty()) {
                    menuItems.filter { it.itemName.toLowerCase(Locale.getDefault()).contains(searchText) }
                } else {
                    menuItems // Show all items if search text is empty
                }
                menuAdapter.submitList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        })


        // Listen for changes in the CurrentTotalCoins field
        currentUserDocument.addSnapshotListener { documentSnapshot, _ ->
            val currentTotalCoins = documentSnapshot?.getLong("CurrentTotalCoins")
            currentTotalCoins?.let {
                binding.textViewUserDetails.text = "$it hushh coins"
            }
        }

        // Fetch and display total order volume
        fetchTotalOrderVolume(contact, name, CardName)
    }

    private fun fetchMenuItems() {
        firestore.collection("business_menu")
            .document("OAC Canteen")
            .collection("menu")
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                menuItems = mutableListOf<HushhMenuItem>() // Initialize the class-level menuItems property

                querySnapshot?.documents?.forEach { document ->
                    val itemName = document.getString("itemName") ?: ""
                    val itemPrice = document.getString("itemPrice") ?: ""
                    val itemStock = document.getString("itemStock") ?: ""

                    val menuItem = HushhMenuItem(itemName, itemPrice, itemStock)
                    menuItems.add(menuItem)
                }

                menuAdapter.submitList(menuItems)
            }
    }


    private fun fetchTotalOrderVolume(contact: String?, name: String?, CardName: String) {
        val orderCollection = currentUserDocument.collection("ordersfrommenu")

        // Listen for changes in the "ordersfrommenu" collection
        orderCollection.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                // Handle failure
                binding.textView33.text = "Total Order Volume: Error"
                binding.textView34.text = "Whoo!! You got Discount worth Rs 0"
                Log.e(TAG, "Error getting documents: ", exception)
                return@addSnapshotListener
            }

            var totalOrderVolume = 0
            for (document in querySnapshot!!) {
                val totalItemCost = document.getString("totalItemCost")?.toIntOrNull() ?: 0
                totalOrderVolume += totalItemCost
            }

            // Set the total order volume to the TextView
            binding.textView33.text = "Total Order Volume: Rs $totalOrderVolume"

            // Calculate discounted cost
            //val discountedCost = (totalOrderVolume.toDouble() * 0.1).coerceAtMost(50.0).toInt()
            val discountedCost = 0
            binding.textView34.text = "Whoo!! You got Discount worth Rs $discountedCost"

            binding.button2.setOnClickListener {
                val coins = binding.textViewUserDetails.text.toString().split(" ")[0].toIntOrNull()
                if (coins != null && coins >= discountedCost * 10) {
                    navigateusertocheckoutmainact(totalOrderVolume,discountedCost, contact, name, CardName)
                } else {
                    Toast.makeText(this, "You don't have sufficient coins.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateusertocheckoutmainact(totalOrderVolume:Int,discountedCost: Int, contact: String?, name: String?, CardName: String) {
        val intent = Intent(this, CheckoutMenuAct::class.java)
        intent.putExtra("contact", contact)
        intent.putExtra("name", name)
        intent.putExtra("CardName", CardName)
        intent.putExtra("totalOrderVolume",totalOrderVolume)
        intent.putExtra("discountedCost",discountedCost)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MenuActivity"
    }
}
