package com.example.project_gemini

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_gemini.model.UserModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.*

class Confirm_Info : AppCompatActivity() {

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var emailAddressEditText: TextInputEditText
    private lateinit var birthdayEditText: TextInputEditText
    private var countryCode: String? = null

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_info)

        // Initialize TextInputEditTexts
        firstNameEditText = findViewById(R.id.FirstName)
        lastNameEditText = findViewById(R.id.LastName)
        phoneNumberEditText = findViewById(R.id.PhoneNumber)
        emailAddressEditText = findViewById(R.id.EmailAddress)
        birthdayEditText = findViewById(R.id.Birthday)

        val intent = intent
        if (intent != null) {
            val userFirstName = intent.getStringExtra("USER_FIRST_NAME")
            val userLastName = intent.getStringExtra("USER_LAST_NAME")
            val userEmail = intent.getStringExtra("USER_EMAIL")
            val receivedPhoneNumber = intent.getStringExtra("phoneNumber")
            countryCode = intent.getStringExtra("COUNTRY_CODE")

            firstNameEditText.setText(userFirstName)
            lastNameEditText.setText(userLastName)
            emailAddressEditText.setText(userEmail)

            if (receivedPhoneNumber != null) {
                // Commented out non-essential Toast
                // Toast.makeText(this, "Fetched Phone Number: $receivedPhoneNumber", Toast.LENGTH_SHORT).show()
                phoneNumberEditText.setText(receivedPhoneNumber)
            }
        }

        val imageView = findViewById<ImageButton>(R.id.button4)

        imageView.setOnClickListener {
            storeUserData()
        }

        phoneNumberEditText.setOnClickListener {
            showCountryCodeToast()
        }

        birthdayEditText.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showCountryCodeToast() {
        showToast("Please don't provide the country code with your contact.") // Important: Inform the user about formatting input
    }

    private fun storeUserData() {
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val emailAddress = emailAddressEditText.text.toString()
        val birthday = birthdayEditText.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || emailAddress.isEmpty() || birthday.isEmpty()) {
            showToast("All fields are required") // Important: Inform the user about missing input
            return
        }

        // Include the country code in the phone number
        val formattedPhoneNumber = "+$countryCode$phoneNumber"

        // Ensure only one plus sign at the beginning
        val cleanedPhoneNumber = formattedPhoneNumber.replace("^\\++".toRegex(), "+")

        // Create a UserModel instance
        val userModel = UserModel(firstName, lastName, cleanedPhoneNumber, emailAddress, birthday)

        // Store user data in Firebase Firestore
        storeInFirestore(userModel, cleanedPhoneNumber)
    }

    private fun storeInFirestore(userModel: UserModel, phoneNumber: String) {
        // Store user data in Firestore
        firestore.collection("users")
            .document(phoneNumber) // Use the provided phoneNumber directly
            .set(userModel)
            .addOnSuccessListener {
                showToast("User details saved with contact $phoneNumber in Firebase Firestore") // Important: Inform the user of successful data storage
                // Store user data locally using SharedPreferences
                saveToSharedPreferences(userModel)
                addUserToSupabase(userModel)
                // Open the login activity
                val hushhHomeIntent = Intent(this, Hushh_Home_Screen::class.java)
                hushhHomeIntent.putExtra("PHONE_NUMBER", userModel.phoneNumber)  // Pass the user's phone number
                startActivity(hushhHomeIntent)
            }


            .addOnFailureListener {
                showToast("Something went wrong") // Important: Inform the user of failure
            }
    }

    private fun getUserUid(): String? {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        return sharedPreferences.getString("userId", null)
    }
    private fun addUserToSupabase(userModel: UserModel) {
        val uid = getUserUid()

        Log.d("Supabase", "User UID: $uid")

        if (uid == null) {
            Log.e("Supabase", "UID is null, cannot proceed")
            showToast("User UID is null, cannot save to Supabase")
            return
        }

        // Construct JSON data
        val json = """
        {
            "id": "$uid",
            "first_name": "${userModel.firstName}",
            "last_name": "${userModel.lastName}",
            "phone_number": "${userModel.phoneNumber}",
            "email_address": "${userModel.emailAddress}",
            "birthday": "${userModel.birthday}"
        }
    """.trimIndent()

        Log.d("Supabase", "JSON Data: $json")

        // Correct Supabase endpoint
        SupabaseClient.postRequest("hush_for_students_v01", json, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Supabase", "Failed to add user to Supabase: ${e.message}")
                runOnUiThread { showToast("Failed to add user to Supabase: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("Supabase", "Failed to add user to Supabase: ${response.code} ${response.message}")
                        runOnUiThread { showToast("Failed to add user to Supabase: ${response.message}") }
                        return
                    }

                    val responseBody = response.body?.string()
                    Log.d("Supabase", "Response Body: $responseBody")

                    runOnUiThread { showToast("User added to Supabase successfully") }
                }
            }
        })
    }




    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = "$year-${month + 1}-$day"
                birthdayEditText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveToSharedPreferences(userModel: UserModel) {
        val sharedPreferences = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("FIRST_NAME", userModel.firstName)
        editor.putString("LAST_NAME", userModel.lastName)
        editor.putString("PHONE_NUMBER", userModel.phoneNumber)
        editor.putString("EMAIL_ADDRESS", userModel.emailAddress)
        editor.putString("BIRTHDAY", userModel.birthday)

        editor.apply()
    }
}
object SupabaseClient {
    private const val SUPABASE_URL = "https://rpmzykoxqnbozgdoqbpc.supabase.co/rest/v1/"
    private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJwbXp5a294cW5ib3pnZG9xYnBjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDE5Mjc5NzEsImV4cCI6MjAxNzUwMzk3MX0.3GwG8YQKwZSWfGgTBEEA47YZAZ-Nr4HiirYPWiZtpZ0" // Replace with your actual Supabase API Key
    private const val SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJwbXp5a294cW5ib3pnZG9xYnBjIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTcwMTkyNzk3MSwiZXhwIjoyMDE3NTAzOTcxfQ.chnzqTCllX3uazNbArr9cmhCIIUUq2YUddGwJ3E7L5E" // Replace with your Supabase service role key

    private val client: OkHttpClient = OkHttpClient()

    // Function to perform HTTP POST request to Supabase
    fun postRequest(endpoint: String, jsonBody: String, callback: Callback) {
        val url = "$SUPABASE_URL$endpoint"
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer $SERVICE_ROLE_KEY")
            .build()

        client.newCall(request).enqueue(callback)
    }
}