package com.example.project_gemini

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.project_gemini.R.layout.custom_location_dialog
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

class Register_Activity : AppCompatActivity() {

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var auth: FirebaseAuth
    private lateinit var countryCodeSpinner: TextView

    private val countryCodes = mapOf(
        "IN" to "91",
        "US" to "1",
        "CN" to "86",
        "BR" to "55",
        "RU" to "7",
        "ID" to "62",
        "PK" to "92",
        "NG" to "234",
        "BD" to "880",
        "MX" to "52",
        "JP" to "81",
        "ET" to "251",
        "PH" to "63",
        "EG" to "20",
        "VN" to "84",
        "CD" to "243",
        "TR" to "90",
        "IR" to "98",
        "DE" to "49",
        "FR" to "33",
        // Additional countries
        "IT" to "39",
        "AR" to "54",
        "CO" to "57",
        "TH" to "66",
        "ZA" to "27",
        "KE" to "254",
        "CA" to "1",
        "AU" to "61",
        "ES" to "34",
        "NL" to "31",
        "SA" to "966",
        "MY" to "60",
        "VE" to "58",
        "PE" to "51",
        "CL" to "56",
        "IQ" to "964",
        "SE" to "46",
        "PL" to "48",
        "PT" to "351",
        "BE" to "32",
        "CZ" to "420",
        "RO" to "40",
        "HU" to "36",
        "CH" to "41"
        // Add more countries as needed
    )





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is granted, fetch the country code
            fetchCountryCode()
        } else {
            // If permission is not granted, request it

            showLocationAccessDialog()
        }

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner)

        // Assuming your ImageButton has the id "cardButton"
        val imageView = findViewById<ImageButton>(R.id.cardButton)

        // Set a click listener for the ImageButton
        imageView.setOnClickListener {
            // Handle the click event, initiate phone number verification
            startPhoneNumberVerification()


        }

        // Configure Google Sign-In
        configureGoogleSignIn()

        // Assuming your Google Sign-In ImageButton has the id "option21"
        findViewById<TextView>(R.id.option21).setOnClickListener {
            signInWithGoogle()
        }
    }

    // Inside the fetchCountryCode function or where you request location permission
    private fun showLocationAccessDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_location_dialog) // Assuming you have a layout file named custom_location_dialog.xml

        val title = dialog.findViewById<TextView>(R.id.dialogTitle)
        val message = dialog.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialog.findViewById<ImageButton>(R.id.dialogPositiveButton)
        val negativeButton = dialog.findViewById<ImageButton>(R.id.dialogNegativeButton)


        title.text = "Location Access"
        message.text = "We need your location for better service."
        title.textSize = 14f
        message.textSize = 12f

        positiveButton.setOnClickListener {
            // Handle the positive button click (granting permission)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            // Handle the negative button click (denying permission)
            showToast("Location permission denied. Defaulting to +91.")
            // Default to +91 or any default country code
            countryCodeSpinner.text = "+91"
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun fetchCountryCode() {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val lastKnownLocation = getLastKnownLocation()
            if (lastKnownLocation != null) {
                val addresses = geocoder.getFromLocation(
                    lastKnownLocation.latitude,
                    lastKnownLocation.longitude,
                    1
                )
                if (addresses!!.isNotEmpty()) {
                    val countryCode = addresses?.get(0)?.countryCode
                    // Use the country code pair map to set the appropriate value
                    countryCodeSpinner.text = countryCodes[countryCode] ?: countryCode
                } else {
                    showToast("Unable to fetch country code")
                }
            } else {
                showToast("Last known location is null. Unable to fetch country code.")
            }
        } catch (e: Exception) {
            showToast("Error fetching country code")
            e.printStackTrace()
        }
    }

    // Helper function to get the last known location
    private fun getLastKnownLocation(): android.location.Location? {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: android.location.Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    // Found best last known location: %s", l);
                    bestLocation = l
                }
            }
            bestLocation
        } catch (e: SecurityException) {
            showToast("Location permission not granted.")
            null
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch the country code
                fetchCountryCode()
            } else {
                showToast("Location permission denied. Defaulting to +91.")
                // Default to +91 or any default country code
                countryCodeSpinner.text = "+91"
            }
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this) { connectionResult ->
                showToast("Google Play services error.")
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // onActivityResult method to handle the result of Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result!!.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account?.idToken)
            } else {
                showToast("Google Sign-In failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get user information
                    val user = auth.currentUser
                    val userName = user?.displayName
                    val userEmail = user?.email

                    showToast("Welcome $userName to Hushh!")

                    // Store user information in SharedPreferences or pass it to the next activity
                    storeUserData(user)

                    // Navigate to Confirm_Info activity
                    navigateToConfirmInfo()
                } else {
                    showToast("Google Sign-In failed.")
                }
            }
    }

    private fun storeUserData(user: FirebaseUser?) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Store user data
        editor.putString("userId", user?.uid)
        editor.putString("userName", user?.displayName)
        editor.putString("userEmail", user?.email)
        editor.putString("userPhotoUrl", user?.photoUrl?.toString())

        // Apply changes
        editor.apply()
    }



    private fun navigateToConfirmInfo() {
        val user = auth.currentUser
        val userName = user?.displayName
        val userEmail = user?.email
        val userFirstName = user?.displayName?.split(" ")?.getOrNull(0)
        val userLastName = user?.displayName?.split(" ")?.getOrNull(1)

        // Get the country code
        val countryCode = countryCodeSpinner.text.toString()

        val intent = Intent(this@Register_Activity, Confirm_Info::class.java).apply {
            putExtra("USER_FIRST_NAME", userFirstName)
            putExtra("USER_LAST_NAME", userLastName)
            putExtra("USER_EMAIL", userEmail)
            putExtra("COUNTRY_CODE", countryCode) // Pass the country code
        }
        startActivity(intent)
        finish()
    }







    // Rest of your existing code...


    companion object {
        private const val RC_SIGN_IN = 9001
        private const val REQUEST_LOCATION_PERMISSION = 123
    }

    private fun startPhoneNumberVerification() {
        val userNumber = findViewById<EditText>(R.id.phoneNumberEditText).text.toString()

        // Check if the user provided a phone number
        if (userNumber.isEmpty()) {
            showToast("Please provide a phone number")
            return
        }

        val countryCode = countryCodeSpinner.text.toString()
        sendOtp(userNumber, countryCode)
    }


    private fun sendOtp(number: String, countryCode: String) {
        // Validate and format the phone number
        val formattedPhoneNumber = validateAndFormatPhoneNumber(number, countryCode)

        // If the phone number is valid, proceed with verification
        if (formattedPhoneNumber != null) {


            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(formattedPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            showToast("Invalid phone number format")
        }
    }

    private fun validateAndFormatPhoneNumber(number: String, countryCode: String): String? {
        // Remove any non-digit characters from the phone number
        val sanitizedNumber = number.replace("[^\\d]".toRegex(), "")

        // Check if the sanitized number is not empty
        if (sanitizedNumber.isNotEmpty()) {
            // Log the values for debugging
            Log.d(TAG, "Country Code: $countryCode, Sanitized Number: $sanitizedNumber")

            // Format the phone number in E.164 format
            val formattedPhoneNumber = "+$countryCode${sanitizedNumber.replaceFirst("^0+", "")}"

            // Log the formatted phone number
            Log.d(TAG, "Formatted Phone Number: $formattedPhoneNumber")

            return formattedPhoneNumber
        }

        return null
    }


    // Callbacks for phone number verification
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Your existing code for onVerificationCompleted
            Log.d(TAG, "onVerificationCompleted: $credential")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Your existing code for onVerificationFailed
            showToast("Verification failed: ${e.message}")
            Log.e(TAG, "onVerificationFailed: ${e.message}")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            showToast("OTP sent successfully")
            Log.d(TAG, "onCodeSent: verificationId=$verificationId, token=$token")
            navigateToOTPActivity(verificationId)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToOTPActivity(verificationId: String) {
        val countryCode = countryCodeSpinner.text.toString()

        val intent = Intent(this@Register_Activity, Otp_Activity::class.java).apply {
            putExtra("verificationId", verificationId)
            putExtra("phoneNumber", findViewById<EditText>(R.id.phoneNumberEditText).text.toString())
            putExtra("COUNTRY_CODE", countryCode) // Pass the country code
        }
        startActivity(intent)
        finish()
    }



}
