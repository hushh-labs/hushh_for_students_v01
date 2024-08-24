package com.example.project_gemini

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ShowFinalCardAct : AppCompatActivity() {

    private val TAG = "ShowFinalCardAct"

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var cardView: View

    private lateinit var surpriseAnimation: LottieAnimationView

    private var firstName: String? = null
    private var lastName: String? = null
    private var emailAddress: String? = null
    private var dob: String? = null
    private var titlenew: String? = null

    private var cardName: String? = null
    private var contactNumber: String? = null

    private lateinit var imageViewQR: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_final_card2)

        cardView = findViewById(R.id.card)
        surpriseAnimation = findViewById(R.id.surprise)

        // Get title and contact number from intent
        titlenew = intent.getStringExtra("title")
        contactNumber = intent.getStringExtra("contactNumber")
        imageViewQR = findViewById(R.id.imageView18)

        // Toast.makeText(this, "Title: $titlenew", Toast.LENGTH_SHORT).show() // Commented out

        contactNumber?.let {
            // Toast.makeText(this, "Contact Number: $it", Toast.LENGTH_SHORT).show() // Commented out

            cardName = titlenew

            // Initialize loader dialog
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Adding card into hushh wallet...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            // Load image from Firestore
            loadCardImage(titlenew)
        } ?: run {
            // Dismiss the progressDialog if contactNumber is null
            progressDialog.dismiss()
            Toast.makeText(this, "Contact Number not found", Toast.LENGTH_SHORT).show() // Urgent Toast
        }
    }

    private fun loadCardImage(title: String?) {
        if (title != null) {
            firestore.collection("Insurance_Cards_Without_Material")
                .document(title)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val imageUrl = documentSnapshot.getString("img")
                    imageUrl?.let {
                        // Use Picasso to load the image into the ImageView
                        val cardImageView = findViewById<ImageView>(R.id.cardImage)

                        // Use Picasso with callback to ensure the image is loaded before proceeding
                        Picasso.get().load(it).into(cardImageView, object : Callback {
                            override fun onSuccess() {
                                // Image loaded successfully, proceed to loadUserInfo
                                loadUserInfo(contactNumber!!)
                            }

                            override fun onError(e: Exception?) {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    this@ShowFinalCardAct,
                                    "Error loading image",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(TAG, "Error loading image", e)
                            }
                        })
                    } ?: run {
                        progressDialog.dismiss() // Dismiss loader on failure
                        // Toast.makeText(this, "Image URL not found", Toast.LENGTH_SHORT).show() // Commented out
                        Log.e(TAG, "Image URL not found")
                    }
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss() // Dismiss loader on failure
                    // Toast.makeText(this, "Failed to fetch image", Toast.LENGTH_SHORT).show() // Commented out
                    Log.e(TAG, "Failed to fetch image", exception)
                }
        } else {
            progressDialog.dismiss() // Dismiss loader if title is null
            // Toast.makeText(this, "Title not found", Toast.LENGTH_SHORT).show() // Commented out
            Log.e(TAG, "Title not found")
        }
    }

    private fun loadUserInfo(contactNumber: String) {
        if (contactNumber != null) {
            firestore.collection("users")
                .document(contactNumber)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    // Assign values to class-level variables
                    firstName = documentSnapshot.getString("firstName")
                    lastName = documentSnapshot.getString("lastName")
                    emailAddress = documentSnapshot.getString("emailAddress")
                    dob = documentSnapshot.getString("birthday")

                    // Check if user information is loaded successfully
                    if (firstName != null && lastName != null && emailAddress != null) {
                        // Set user information in TextViews
                        val textViewName = findViewById<TextView>(R.id.textViewName)
                        val textViewContact = findViewById<TextView>(R.id.textViewContact)
                        val textViewEmail = findViewById<TextView>(R.id.textViewemail)

                        textViewName.text = "$firstName $lastName"
                        textViewContact.text = "$contactNumber"
                        textViewEmail.text = emailAddress

                        // Calculate age
                        val age = calculateAge(dob)

                        val webAppUrl = "https://hush8-pay.web.app/"
                        val qrCodeData =
                            "$webAppUrl?firstName=${textViewName.text.toString()}" +
                                    "&lastName=" +
                                    "&phoneNumber=${textViewContact.text.toString()}" +
                                    "&emailAddress=${textViewEmail.text.toString()}" +
                                    "&cardName=${cardName}"

                        generateQRCode(qrCodeData, imageViewQR)

                        // Call saveDataToFirebaseRealtimeDatabase only if user information is loaded
                        saveDataToFirebaseRealtimeDatabase(cardName.toString(), contactNumber)
                    } else {
                        progressDialog.dismiss() // Dismiss loader if user information is not loaded
                        // Toast.makeText(this, "Failed to fetch user information", Toast.LENGTH_SHORT).show() // Commented out
                        Log.e(TAG, "Failed to fetch user information")
                    }
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss() // Dismiss loader on failure
                    // Toast.makeText(this, "Failed to fetch user information", Toast.LENGTH_SHORT).show() // Commented out
                    Log.e(TAG, "Failed to fetch user information", exception)
                }
        } else {
            progressDialog.dismiss() // Dismiss loader if contactNumber is null
            Toast.makeText(this, "Contact Number not found", Toast.LENGTH_SHORT).show() // Urgent Toast
            Log.e(TAG, "Contact Number not found")
        }
    }

    private fun generateQRCode(data: String, imageViewQR: ImageView) {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 500, 500)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imageViewQR.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun calculateAge(dob: String?): Int {
        if (dob != null) {
            val dobDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dob)
            if (dobDate != null) {
                val currentDate = Date()
                val ageInMillis = currentDate.time - dobDate.time
                val ageInYears = ageInMillis / (365.25 * 24 * 60 * 60 * 1000).toLong()
                return ageInYears.toInt()
            }
        }
        return 0
    }

    private fun saveDataToFirebaseRealtimeDatabase(cardName: String?, contactNumber: String) {
        if (cardName != null && firstName != null && lastName != null && emailAddress != null) {
            // Take a screenshot of the card view
            // Toast.makeText(this, "Card Name: $cardName", Toast.LENGTH_SHORT).show() // Commented out
            val bitmap = getBitmapFromView(cardView)

            if (bitmap != null) {
                // Upload the screenshot to Firebase Storage
                val storageReference = FirebaseStorage.getInstance().reference
                val imageRef = storageReference.child("card_screenshots/${UUID.randomUUID()}.png")

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val data = byteArrayOutputStream.toByteArray()

                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // Get the download URL of the uploaded image
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        // Save data in Firebase Realtime Database
                        val databaseReference = FirebaseDatabase.getInstance().reference
                            .child("users")
                            .child(contactNumber)
                            .child(cardName)

                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val currentDateAndTime: String = sdf.format(Date())

                        databaseReference.child("emailAddress").setValue(emailAddress)
                        databaseReference.child("firstName").setValue(firstName)
                        databaseReference.child("lastName").setValue(lastName)
                        databaseReference.child("phoneNumber").setValue(contactNumber)
                        databaseReference.child("imageURL").setValue(imageUrl)
                        databaseReference.child("timestamp").setValue(currentDateAndTime)

                        surpriseAnimation.visibility = View.VISIBLE
                        surpriseAnimation.playAnimation()

                        cardView.visibility = View.GONE

                        // Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show() // Commented out
                        progressDialog.dismiss()

                        // Delay for 2 seconds
                        Handler().postDelayed({
                            // Hide surprise animation after 2 seconds
                            surpriseAnimation.visibility = View.GONE

                            // Pass intent to ShowHomeCardAct activity
                            val intent = Intent(this@ShowFinalCardAct, ShowHomeCardAct::class.java)
                            intent.putExtra("imageURL", imageUrl)
                            intent.putExtra("name", "$firstName $lastName")
                            intent.putExtra("email", emailAddress)
                            intent.putExtra("dob", dob)
                            intent.putExtra("parentName", titlenew)
                            intent.putExtra("globalPhoneNumber", contactNumber)
                            startActivity(intent)
                            finish()
                        }, 4000)
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to get image download URL", Toast.LENGTH_SHORT).show() // Urgent Toast
                        Log.e(TAG, "Failed to get image download URL", exception)
                        progressDialog.dismiss()
                    }
                }.addOnFailureListener { exception ->
                    // Toast.makeText(this, "Failed to upload screenshot", Toast.LENGTH_SHORT).show() // Commented out
                    Log.e(TAG, "Failed to upload screenshot", exception)
                    progressDialog.dismiss()
                }
            } else {
                // Toast.makeText(this, "Bitmap is null", Toast.LENGTH_SHORT).show() // Commented out
                Log.e(TAG, "Bitmap is null")
                progressDialog.dismiss()
            }
        } else {
            // Toast.makeText(this, "Some user information is missing", Toast.LENGTH_SHORT).show() // Commented out
            Log.e(TAG, "Some user information is missing")
            progressDialog.dismiss()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
