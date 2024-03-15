package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_gemini.databinding.ActivityHushhCoinsEarnedBinding
import com.google.firebase.firestore.FirebaseFirestore

class HushhCoinsEarnedAct : AppCompatActivity() {

    private lateinit var binding: ActivityHushhCoinsEarnedBinding
    private lateinit var historyCoinsAdapter: HistoryCoinsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHushhCoinsEarnedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from the intent
        val coinEarned: String? = intent.getStringExtra("COIN_EARNED")
        val name: String? = intent.getStringExtra("NAME")
        val user_contact: String? = intent.getStringExtra("CONTACT")

        // Check if the data is not null before displaying
        if (coinEarned != null && name != null) {
            // Display the data using View Binding

            binding.textView11.text = "Hi $name,"

            // Divide the total coinEarned by 10 and display
            //val dividedAmount = coinEarned.toIntOrNull()?.div(10)
            //binding.textView10.text = "Rs ${dividedAmount ?: 0}"
        } else {
            // Handle the case when data is null
            Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show()
        }

        // Set click listener for imageView4
        binding.imageView4.setOnClickListener {
            // Start the HushhHomeAct activity
            val intent = Intent(this, Hushh_Home_Screen::class.java)
            startActivity(intent)
        }

        // Set a click listener for imageView11
        binding.imageView11.setOnClickListener {
            // Start JoinMissionAct activity with the intent
            val joinMissionIntent = Intent(this, JoinMissionAct::class.java)
            joinMissionIntent.putExtra("CONTACT", intent.getStringExtra("CONTACT"))
            joinMissionIntent.putExtra("COIN_EARNED", coinEarned)
            joinMissionIntent.putExtra("NAME", name)
            startActivity(joinMissionIntent)
        }

        binding.imageView15.setOnClickListener{
            val joinMissionIntent = Intent(this, CoinsDashboard::class.java)
            joinMissionIntent.putExtra("CONTACT", intent.getStringExtra("CONTACT"))
            joinMissionIntent.putExtra("COIN_EARNED", coinEarned)
            joinMissionIntent.putExtra("NAME", name)
            startActivity(joinMissionIntent)
        }

        binding.imageView19.setOnClickListener{
            val joinMissionIntent = Intent(this, ManageCoinsAct::class.java)
            joinMissionIntent.putExtra("CONTACT", intent.getStringExtra("CONTACT"))
            joinMissionIntent.putExtra("COIN_EARNED", coinEarned)
            joinMissionIntent.putExtra("NAME", name)
            startActivity(joinMissionIntent)
        }

        val contact: String? = intent.getStringExtra("CONTACT")

        if (contact != null) {
            // Fetch data from Firebase
            fetchDataFromFirebase(contact)
        } else {
            Toast.makeText(this, "Contact is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDataFromFirebase(contact: String) {
        val db = FirebaseFirestore.getInstance()

        // Assuming your Firestore structure
        val path = "users/$contact/coins/hushhcoins"

        // Path to hushhcoins
        val pathforhushhcoins = "users/$contact"

        // Path to fetch user data
        val pathforUserData = "users/$contact"


        db.document(pathforhushhcoins).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentTotalCoins = documentSnapshot.getLong("CurrentTotalCoins")
                    if (currentTotalCoins != null) {
                        // Format the coins
                        val formattedCoins = String.format("%,.2f", currentTotalCoins.toDouble())

                        // Update the TextView with the formatted coins
                        binding.textView44.text = formattedCoins
                    } else {
                        Toast.makeText(this, "CurrentTotalCoins is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching data: $e", Toast.LENGTH_SHORT).show()
            }

        db.document(pathforUserData).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Retrieve user data from Firestore
                    val firstName = documentSnapshot.getString("firstName")
                    val lastName = documentSnapshot.getString("lastName")
                    val emailAddress = documentSnapshot.getString("emailAddress")
                    val phoneNumber = documentSnapshot.getString("phoneNumber")

                    // Set the retrieved data to the corresponding TextViews
                    binding.textView52.text = firstName
                    binding.textView53.text = lastName
                    binding.textView55.text = emailAddress
                    binding.textView57.text = phoneNumber
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user data: $e", Toast.LENGTH_SHORT).show()
            }


        db.document(path).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val coinList = mutableListOf<HistoryCoins>()

                    // Extract data and add to the list
                    for (field in documentSnapshot.data.orEmpty()) {
                        val fieldName = field.key
                        val fieldValue = field.value.toString()

                        // Skip the field with the name "App_engagement_timestamp"
                        if (fieldName != "App_engagement_timestamp") {
                            // Remove unwanted prefix from contact
                            val cleanFieldName = fieldName.substringAfterLast("/", fieldName)

                            val prefix = if (fieldValue.toIntOrNull() ?: 0 >= 0) "+" else ""
                            coinList.add(HistoryCoins(cleanFieldName, "$prefix$fieldValue"))

                        }
                    }

                    // Set up RecyclerView
                    historyCoinsAdapter = HistoryCoinsAdapter(coinList)
                    binding.hushhcoinhistory.adapter = historyCoinsAdapter

                    // Optional: Set layout manager if needed
                    val layoutManager = LinearLayoutManager(this)
                    binding.hushhcoinhistory.layoutManager = layoutManager
                } else {
                    Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching data: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
