package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class Otp_Activity : AppCompatActivity() {

    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private var countryCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val imageView = findViewById<ImageButton>(R.id.button_otp_validation)

        verificationId = intent.getStringExtra("verificationId")
        phoneNumber = intent.getStringExtra("phoneNumber")
        countryCode = intent.getStringExtra("COUNTRY_CODE")


        // Set a click listener for the ImageView
        imageView.setOnClickListener {
            // Handle the click event, validate OTP
            validateOTP()
        }
    }

    private fun validateOTP() {
        val userOTP = findViewById<EditText>(R.id.userOTP).text.toString()

        if (userOTP.isEmpty()) {
            showToast("Please enter the OTP")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId!!, userOTP)
        signInWithPhoneAuthCredential(credential)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Verification successful")
                    navigateToNextPage()
                } else {
                    showToast("Verification failed")
                }
            }
    }

    private fun navigateToNextPage() {
        // Intent to start the next activity
        val intent = Intent(this, Confirm_Info::class.java)
        intent.putExtra("verificationId", verificationId)
        intent.putExtra("phoneNumber", phoneNumber)
        intent.putExtra("COUNTRY_CODE", countryCode)
        startActivity(intent)
        // Finish the current activity if needed
        finish()
    }
}