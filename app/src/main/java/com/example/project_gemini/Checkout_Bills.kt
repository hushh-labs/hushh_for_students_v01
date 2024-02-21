package com.example.project_gemini

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.project_gemini.databinding.ActivityCheckoutBillsBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.OutputStream

class Checkout_Bills : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBillsBinding
    private lateinit var title: String
    private lateinit var contactNumber: String
    private var currentDocumentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from intent
        title = intent.getStringExtra("parentName") ?: ""
        contactNumber = intent.getStringExtra("globalPhoneNumber") ?: ""

        // Use the retrieved data as needed
        val toastMessage = "Parent Name: $title\nContact Number: $contactNumber"
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()

        fetchDataFromFirestore(title, contactNumber)

        binding.declinebtn.setOnClickListener {
            // Check if the currentDocumentId is not null
            if (!currentDocumentId.isNullOrBlank()) {
                // Call the delete function with the currentDocumentId
                deleteDocumentFromFirestore(currentDocumentId!!)
            } else {
                Toast.makeText(this, "No item to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteDocumentFromFirestore(documentId: String) {
        val brandname = title

        val firestore = FirebaseFirestore.getInstance()
        val phoneNumber = contactNumber

        // Reference to "users" collection
        val usersCollection = firestore.collection("users").document(phoneNumber)

        // Reference to "buisness_onboard" collection
        val buisnessOnboardCollection = firestore.collection("buisness_onboard")

        // Reference to "invoice" subcollection under "buisness_onboard" collection
        val invoiceCollectionRef = buisnessOnboardCollection.document(brandname).collection("invoice")

        // Reference to "orders" collection under the user
        val ordersCollectionRef = usersCollection.collection(brandname).document("orders")
            .collection("${brandname}orders")

        // Delete the document from "invoice" subcollection
        invoiceCollectionRef.document(documentId)
            .delete()
            .addOnSuccessListener {
                // Document deleted from "invoice" subcollection, now delete from "orders" collection
                deleteFromOrdersCollection(ordersCollectionRef, documentId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error deleting item from 'invoice' collection: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFromOrdersCollection(ordersCollectionRef: CollectionReference, documentId: String) {
        // Delete the document from "orders" collection
        ordersCollectionRef.document(documentId)
            .delete()
            .addOnSuccessListener {
                // Document deleted from "orders" collection, now delete from "buisness_onboard" collection
                deleteFromBuisnessOnboardCollection(documentId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error deleting item from 'orders' collection: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFromBuisnessOnboardCollection(documentId: String) {
        val brandname = title

        val firestore = FirebaseFirestore.getInstance()

        // Reference to "buisness_onboard" collection
        val buisnessOnboardCollection = firestore.collection("buisness_onboard")

        // Delete the document from "buisness_onboard" collection
        buisnessOnboardCollection.document(brandname)
            .collection("invoice")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                // Document deleted from "buisness_onboard" collection, show success message
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error deleting item from 'buisness_onboard' collection: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun fetchDataFromFirestore(title: String, contactNumber: String) {
        val brandname = title

        // Firestore reference
        val firestore = FirebaseFirestore.getInstance()
        val phoneNumber = contactNumber

        val collectionReference = firestore.collection("users").document(phoneNumber)
            .collection(brandname).document("orders")
            .collection("${brandname}orders")

        // Add a snapshot listener to get real-time updates
        collectionReference.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle errors
                Toast.makeText(this, "Error fetching data from Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                // Get the most recent document
                val mostRecentDocument = snapshot.documents[snapshot.documents.size - 1]

                currentDocumentId = mostRecentDocument.id

                // Listen for changes in the PaymentConfirmation field
                mostRecentDocument.getReference().addSnapshotListener { paymentConfirmationSnapshot, _ ->
                    if (paymentConfirmationSnapshot != null) {
                        val paymentConfirmation = paymentConfirmationSnapshot.getString("PaymentConfirmation")

                        if (paymentConfirmation == "yes") {
                            // PaymentConfirmation is already "yes"
                            // Display a thank you message or any appropriate message
                            Toast.makeText(this, "Thank you for your order!", Toast.LENGTH_SHORT).show()

                            // Check if imageView29 has an image, and remove it if present
                            if (binding.imageView29.drawable != null) {
                                binding.imageView29.setImageDrawable(null)
                                binding.textView20.text = "Thank you for your order!"
                            }
                        } else if (paymentConfirmation == "no") {
                            // Continue with further steps
                            val orderId = mostRecentDocument.getString("OrderId")

                            // Retrieve OrderTotal as a number
                            val orderTotal = mostRecentDocument.getDouble("OrderTotal") ?: 0.0
                            val formattedOrderTotal = String.format("%.2f", orderTotal) // Format as currency if needed

                            val hushhDiscount = mostRecentDocument.getString("HushhDiscount")
                            val billUrl = mostRecentDocument.getString("BillUrl")

                            // Display Toast messages for each field
                            Toast.makeText(this, "OrderId: $orderId", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, "OrderTotal: $formattedOrderTotal", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, "HushhDiscount: $hushhDiscount", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, "BillUrl: $billUrl", Toast.LENGTH_SHORT).show()

                            // Extract final cost from UserPaid field
                            val userPaid = mostRecentDocument.getString("UserPaid") ?: ""
                            val finalCost = userPaid.split("=")[1].trim()

                            binding.textView20.text = "Payment Request of $finalCost"

                            // Display final cost
                            Toast.makeText(this, "Final Cost: $finalCost", Toast.LENGTH_SHORT).show()

                            // Load BillUrl using Glide
                            Glide.with(this)
                                .load(billUrl)
                                .apply(RequestOptions().placeholder(R.drawable.logo_splash_screen))
                                .into(binding.imageView29)

                            // Set click listener for imageView30
                            binding.imageView30.setOnClickListener {
                                downloadImageToGallery(this, (binding.imageView29.drawable as BitmapDrawable).bitmap)
                            }
                        }
                    }
                }

            } else {
                // No documents found
                Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show()
            }
        }
    }





    private fun downloadImageToGallery(context: Context, bitmap: Bitmap) {
        val resolver: ContentResolver = context.contentResolver
        val imageFileName = "Invoice_${System.currentTimeMillis()}.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProjectGemini") // Change the folder name as needed
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (imageUri != null) {
            val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                // Notify the gallery about the new image
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri))

                Toast.makeText(context, "Image downloaded to gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
