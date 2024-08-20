package com.example.project_gemini

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_gemini.databinding.ActivityJoinMissionBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class JoinMissionAct : AppCompatActivity() {

    private lateinit var binding: ActivityJoinMissionBinding
    private var isButtonClickable = true
    private var contact: String? = null
    private var firestoreListener: ListenerRegistration? = null
    private val checkInterval = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinMissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from the intent
        val name: String? = intent.getStringExtra("NAME")
        val coinEarned: String? = intent.getStringExtra("COIN_EARNED")
        contact = intent.getStringExtra("CONTACT")

        // Check if the data is not null before displaying
        if (name != null && coinEarned != null && contact != null) {
            // Display the data using View Binding
            binding.textView11.text = name
            binding.textViewUserDetails.text = "$coinEarned hushh coins"
        } else {
            // Handle the case when data is null
            // You can show a default value or handle it as per your requirement
        }

//        binding.jm1appengagement.setOnClickListener {
//            handleButtonClick()
//        }

        binding.jm2appengagement.setOnClickListener {
            handleLinkedinButtonClick()
        }

        binding.cm2appengagement.setOnClickListener {
            handleCm2ButtonClick()
        }

        binding.jm4purchaseengagement.setOnClickListener {
            // Open Instagram link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/stumato.app/"))
            startActivity(intent)

            // Update Firestore field "Instagram_Bliss"
            contact?.let { it1 -> updateInstagramBlissField(it1) }
        }

        binding.jm3cardengagement.setOnClickListener {
            // Start NewCardMarketAct and pass the contact information
            val intent = Intent(this, NewCardMarketAct::class.java)
            intent.putExtra("CONTACT", contact)
            startActivity(intent)
        }

//        binding.jm5gamifiedcahllange.setOnClickListener {
//            // Start NewCardMarketAct and pass the contact information
//            val intent = Intent(this, NewCardMarketAct::class.java)
//            intent.putExtra("CONTACT", contact)
//            startActivity(intent)
//        }


        // Listen for real-time updates on timestamp
        listenForTimestampUpdates(contact)

        // Periodically check LinkedIn_Bliss availability
        startPeriodicCheck()
    }

    private fun updateInstagramBlissField(contact: String) {
        val db = FirebaseFirestore.getInstance()

        // Assuming your Firestore structure
        val path = "users/$contact/coins/hushhcoins"

        val updateMap = hashMapOf<String, Any>()

        // Get the current value of "Instagram_Bliss"
        db.document(path)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val currentInstagramBliss = documentSnapshot.getLong("Instagram_Bliss") ?: 0

                // Calculate the new value, ensuring it doesn't exceed 100
                val newInstagramBliss = if (currentInstagramBliss < 100) {
                    currentInstagramBliss + 100
                } else {
                    100
                }

                // Update the "Instagram_Bliss" field
                updateMap["Instagram_Bliss"] = newInstagramBliss

                // Perform the update
                db.document(path)
                    .update(updateMap)
                    .addOnSuccessListener {
                        // Field update success
                        showToast("Instagram_Bliss field updated successfully!")
                    }
                    .addOnFailureListener { e ->
                        // Handle error
                        showToast("Error updating Instagram_Bliss field: $e")
                    }
            }
            .addOnFailureListener { e ->
                // Handle error
                showToast("Error retrieving current Instagram_Bliss value: $e")
            }
    }


    private fun updateFirestoreField(contact: String) {
        val db = FirebaseFirestore.getInstance()

        // Assuming your Firestore structure
        val path = "users/$contact/coins/hushhcoins"

        val updateMap = hashMapOf(
            "App_engagement" to FieldValue.increment(30),
            "App_engagement_timestamp" to FieldValue.serverTimestamp()
        )

        db.document(path)
            .update(updateMap as Map<String, Any>)
            .addOnSuccessListener {
                // Field update success
                showToast("Firestore field updated successfully!")
            }
            .addOnFailureListener { e ->
                // Handle error
                showToast("Error updating Firestore field: $e")
            }
    }

    private fun listenForTimestampUpdates(contact: String?) {
        val db = FirebaseFirestore.getInstance()

        // Assuming your Firestore structure
        val path = "users/$contact/coins/hushhcoins"

        firestoreListener = db.document(path)
            .addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val timestamp = documentSnapshot.getTimestamp("App_engagement_timestamp")

                    if (timestamp != null) {
                        val currentTime = Calendar.getInstance().timeInMillis
                        val timestampTime = timestamp.toDate().time

                        val difference = currentTime - timestampTime
//                        if (difference >= 3600000) {
//                            binding.jm1appengagement.visibility = View.VISIBLE
//                        } else {
//                            binding.jm1appengagement.visibility = View.GONE
//                        }
                    }
                }
            }
    }

    private fun handleButtonClick() {
        if (isButtonClickable) {
            isButtonClickable = false

            // Update Firestore field
            contact?.let { it1 -> updateFirestoreField(it1) }


//            binding.jm1appengagement.visibility = View.GONE

            // After 1 minute, show the ImageView again
            Handler().postDelayed({
                isButtonClickable = true
            }, 60000) // 60000 milliseconds = 1 minute
        }
    }

    private fun handleLinkedinButtonClick() {
        // Redirect to LinkedIn
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/company/hushh-ai/"))
        startActivity(intent)

        // Update Firestore field "LinkedIn_Bliss"
        contact?.let { it1 -> updateLinkedinBlissField(it1) }
    }

    private fun handleCm2ButtonClick() {
        // Check if "LinkedIn_Bliss" is available
        checkLinkedinBlissAvailability(contact)
    }

    private fun updateLinkedinBlissField(contact: String) {
        val db = FirebaseFirestore.getInstance()

        // Assuming your Firestore structure
        val path = "users/$contact/coins/hushhcoins"

        val updateMap = hashMapOf(
            "LinkedIn_Bliss" to 100
        )

        db.document(path)
            .update(updateMap as Map<String, Any>)
            .addOnSuccessListener {
                // Field update success
                showToast("LinkedIn_Bliss field updated successfully!")
            }
            .addOnFailureListener { e ->
                // Handle error
                showToast("Error updating LinkedIn_Bliss field: $e")
            }
    }

    private fun checkLinkedinBlissAvailability(contact: String?) {
        val db = FirebaseFirestore.getInstance()

        // Assuming your Firestore structure
        val path = "users/$contact/coins/hushhcoins"

        db.document(path)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val linkedinBliss = documentSnapshot.getLong("LinkedIn_Bliss")

                if (linkedinBliss != null) {
                    // LinkedIn_Bliss is available
                    binding.jm2appengagement.visibility = View.GONE
                    binding.cm2appengagement.visibility = View.VISIBLE

                }
            }
            .addOnFailureListener { e ->
                // Handle error

            }
    }

    private fun startPeriodicCheck() {
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                // Run your function here
                checkLinkedinBlissAvailability(contact)
                handler.postDelayed(this, checkInterval)
            }
        }
        handler.postDelayed(runnable, checkInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove Firestore listener to avoid memory leaks
        firestoreListener?.remove()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
