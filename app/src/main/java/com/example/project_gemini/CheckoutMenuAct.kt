package com.example.project_gemini

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_gemini.databinding.ActivityCheckoutMenuBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dev.shreyaspatil.easyupipayment.EasyUpiPayment
import dev.shreyaspatil.easyupipayment.listener.PaymentStatusListener
import dev.shreyaspatil.easyupipayment.model.TransactionDetails
import dev.shreyaspatil.easyupipayment.model.TransactionStatus
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class CheckoutMenuAct : AppCompatActivity(), PaymentResultWithDataListener {

    private lateinit var binding: ActivityCheckoutMenuBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var easyUpiPayment: EasyUpiPayment
    private var progressDialog: ProgressDialog? = null
    private val razorpayKey = "rzp_live_08rMlga16zsqdy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieving data from intent
        val contact = intent.getStringExtra("contact")
        val name = intent.getStringExtra("name")
        val CardName = intent.getStringExtra("CardName")
        val totalOrderVolume = intent.getIntExtra("totalOrderVolume", 0)
        val discountedCost = intent.getIntExtra("discountedCost", 0)

        // Setting current timestamp
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
        binding.textView14.text = "$timeStamp"
        binding.textView7.text = "Invoice_$name"

        // Setting current date and time
        val currentDateTime = SimpleDateFormat("dd MMM yyyy h:mm a", Locale.getDefault()).format(Date())
        binding.textView10.text = currentDateTime


        // Calculate delivery charge
        val amountWithoutDelivery = (totalOrderVolume - discountedCost).toFloat()

        val deliveryCharge: Int
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val isLateNight = hour >= 22 || hour < 2

        if (isLateNight) {
            var calculatedDeliveryCharge = (amountWithoutDelivery * 0.10).toInt() // 10% of the total order value

            // Check if calculated delivery charge is less than 5, then set it to 5
            if (calculatedDeliveryCharge < 5) {
                calculatedDeliveryCharge = 5
            }

            deliveryCharge = calculatedDeliveryCharge
        } else {
            deliveryCharge = 0
        }

        // Calculate total amount
        val amount = amountWithoutDelivery + deliveryCharge

        binding.textView36.text = "Rs $deliveryCharge" // Set delivery charge text
        binding.textView20.text = "Rs$totalOrderVolume - Rs$discountedCost + Rs$deliveryCharge = Rs$amount" // Update total amount text




        binding.textView20.text = "Rs$totalOrderVolume - Rs$discountedCost = Rs$amount"
        binding.textView22.text = "Rs$discountedCost"
        binding.textView201.text = "Payment Request of $amount"

        val currentTime = System.currentTimeMillis().toString()

        // Initialize Razorpay checkout
        Checkout.preload(applicationContext)

        // Handle imageView31 click
        binding.imageView31.setOnClickListener {
            // Check if textView15.text is not null
            if (binding.textView15.text != null) {
                startRazorpayPayment(amount, contact)
                Log.d("Payment", "Payment process started")
            } else {
                Toast.makeText(this, "Please add items to your order before proceeding to payment", Toast.LENGTH_SHORT).show()
            }
        }

        //after setting click listener for imageView31
        binding.button.setOnClickListener {
            // Get the current user's document reference
            val currentUserDocument = db.collection("users").document(contact.toString())

            // Retrieve all documents from the "ordersfrommenu" collection
            currentUserDocument.collection("ordersfrommenu")
                .get()
                .addOnSuccessListener { ordersSnapshot ->
                    // Loop through each document snapshot
                    for (document in ordersSnapshot.documents) {
                        // Get the reference to each document and delete it
                        val documentRef = document.reference
                        documentRef.delete()
                            .addOnSuccessListener {
                                // Document successfully deleted
                                Toast.makeText(this, "Document deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Handle any errors
                                Toast.makeText(this, "Error deleting document: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Toast.makeText(this, "Error getting documents: $e", Toast.LENGTH_SHORT).show()
                }
        }


        val currentUserDocument = db.collection("users").document(contact.toString())
        currentUserDocument.collection("ordersfrommenu")
            .addSnapshotListener { ordersSnapshot, e ->
                if (e != null) {
                    // Handle errors
                    return@addSnapshotListener
                }

                val ordersText = StringBuilder()
                var serialNumber = 1

                ordersSnapshot?.forEach { document ->
                    val item = document.id
                    val itemPrice = document.getString("itemPrice") ?: ""
                    val totalItemCost = document.getString("totalItemCost") ?: ""
                    val userInputOfStock = document.getString("userInputOfStock") ?: ""

                    // Append each item details to the ordersText with incremented serial number
                    ordersText.append("$serialNumber. $item Qty:$userInputOfStock Rs$totalItemCost\n")
                    serialNumber++
                }

                // Setting the text to textView15
                binding.textView15.text = ordersText.toString()

            }
    }

    private fun startRazorpayPayment(amount: Float, contact: String?) {
        val razorpay = Checkout()
        razorpay.setKeyID(razorpayKey)

        try {
            val options = JSONObject()
            options.put("name", "hushh for students")
            options.put("description", "Order From Stumato")
            options.put("image", "YOUR_IMAGE_URL") // Replace with your image URL
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt())  // Amount multiplied by 100 as Razorpay expects amount in paise
            options.put("prefill", JSONObject().apply {
                put("email", "example@email.com")  // User's email
                put("contact", contact)  // User's contact number
            })

            razorpay.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun takessamdupload(logMessage: String) {
        showLoadingDialog()
        // Take screenshot of linear layout and save to Firebase Storage
        val linearLayoutInvoice = findViewById<View>(R.id.linearlayoutinvoice)
        val bitmap = getBitmapFromView(linearLayoutInvoice)
        if (bitmap != null) {
            val storageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageReference.child("invoice_screenshots/${UUID.randomUUID()}.png")

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    // Update Firestore with the screenshot URL
                    updateFirestore(imageUrl,bitmap,logMessage)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload screenshot", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to show the loading dialog
    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("hushhing...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    // Function to dismiss the loading dialog
    private fun dismissLoadingDialog() {
        progressDialog?.dismiss()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun updateFirestore(imageUrl: String, bitmap: Bitmap, logMessage: String) {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
        val brandname = intent.getStringExtra("CardName")
        val totalAmount = intent.getIntExtra("totalOrderVolume", 0)
        val discountedCost = intent.getIntExtra("discountedCost", 0)
        val amount = (totalAmount - discountedCost).toFloat()

        // Calculate delivery charge
        val amountWithoutDelivery = (totalAmount - discountedCost).toFloat()

        val deliveryCharge: Int
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val isLateNight = hour >= 22 || hour < 2

        if (isLateNight) {
            var calculatedDeliveryCharge = (amountWithoutDelivery * 0.10).toInt() // 10% of the total order value

            // Check if calculated delivery charge is less than 5, then set it to 5
            if (calculatedDeliveryCharge < 5) {
                calculatedDeliveryCharge = 5
            }

            deliveryCharge = calculatedDeliveryCharge
        } else {
            deliveryCharge = 0
        }



        // Add delivery charge to totalAmount
        val totalAmountWithDelivery = totalAmount + deliveryCharge

        val firestore = FirebaseFirestore.getInstance()
        val contact = intent.getStringExtra("contact")

        // Reference to the 'brandname' subcollection within the user document
        val brandnameCollection = firestore.collection("users").document(contact.toString()).collection(brandname ?: "")

        // Query to fetch all documents within the 'brandname' subcollection
        brandnameCollection.get()
            .addOnSuccessListener { brandnameSnapshot ->
                val questionsAndAnswers = StringBuilder()

                // Iterate through each document within the 'brandname' subcollection
                for (document in brandnameSnapshot.documents) {
                    // Check if the document contains the required fields
                    if (document.contains("question") && document.contains("answer") && document.contains("title")) {
                        val question = document.getString("question") ?: ""
                        val answer = document.getString("answer") ?: ""
                        val title = document.getString("title") ?: ""

                        // Append question and answer to the StringBuilder
                        questionsAndAnswers.append("$title: $question\n")
                        questionsAndAnswers.append("Answer: $answer\n\n") // Add a newline for clarity
                    }
                }

                // Update 'users' collection
                val usersDocument = firestore.collection("users").document(contact.toString())
                    .collection(brandname ?: "").document("${brandname}${timeStamp}")

                usersDocument.set(
                    mapOf(
                        "OrderId" to "${brandname}${timeStamp}",
                        "OrderTotal" to totalAmountWithDelivery,
                        "HushhDiscount" to "Rs ${discountedCost}.00 discount",
                        "BillUrl" to imageUrl,
                        "UserPaid" to String.format(Locale.getDefault(), "Rs %.2f - Rs %.2f = Rs %.2f", totalAmount.toFloat(), discountedCost.toFloat(), amount),
                        "PaymentConfirmation" to "yes",
                        "Timestamp" to timeStamp,
                        "QuestionsAndAnswers" to questionsAndAnswers.toString()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Data saved to 'users' collection successfully", Toast.LENGTH_SHORT).show()

                    // Call openWhatsApp() with the specified phone number and message
                    val name = intent.getStringExtra("name")
                    openWhatsApp("+917276867747",
                        "Order by:\n$name\n" +
                                "OrderId: ${brandname}${timeStamp}\n" +
                                "OrderTotal: $totalAmountWithDelivery\n" +
                                "*transaction details: $logMessage*\n" +
                                "Discount: Rs ${discountedCost}.00 discount\n" +
                                "BillUrl: $imageUrl\n" +
                                "UserPaid: ${String.format(Locale.getDefault(), "Rs %.2f - Rs %.2f = Rs %.2f", totalAmount.toFloat(), discountedCost.toFloat(), amount)}\n" +
                                "UserPhone: ${contact.toString()}\n" +
                                "PaymentConfirmation: yes\n" +
                                "Preferences:\n\n$questionsAndAnswers"
                    )

                    progressDialog?.dismiss()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to save data to 'users' collection", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Toast.makeText(this, "Failed to fetch questions and answers", Toast.LENGTH_SHORT).show()
                progressDialog?.dismiss()
            }
    }



    private fun openWhatsApp(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val url = "https://wa.me/$phoneNumber/?text=${Uri.encode(message)}"
        intent.data = Uri.parse(url)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp not installed on your device", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(paymentId: String?, p1: PaymentData?) {
        // Handle payment success
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        val logMessage = "Payment Successful"

        binding.textView42.text = "Payment Successful"
        takessamdupload(logMessage)

        // Update itemStock in Firestore documents
        val contact = intent.getStringExtra("contact")
        val currentUserDocument = db.collection("users").document(contact.toString())
        currentUserDocument.collection("ordersfrommenu").get()
            .addOnSuccessListener { ordersSnapshot ->
                for (document in ordersSnapshot.documents) {
                    val itemName = document.id
                    val userInputOfStock = document.getString("userInputOfStock")?.toIntOrNull() ?: 0

                    // Update itemStock in business_menu
                    val businessMenuCollection = db.collection("business_menu")
                        .document("OAC Canteen")
                        .collection("menu")
                        .document(itemName)

                    businessMenuCollection.get().addOnSuccessListener { menuDocSnapshot ->
                        val currentStock = menuDocSnapshot.getString("itemStock")?.toIntOrNull() ?: 0
                        val updatedStock = currentStock - userInputOfStock
                        businessMenuCollection.update("itemStock", updatedStock.toString())
                            .addOnSuccessListener {
                                // Toast for successful stock update
                                Toast.makeText(
                                    this,
                                    "$itemName stock updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { exception ->
                                // Toast for failure in updating stock
                                Toast.makeText(
                                    this,
                                    "Failed to update $itemName stock",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                // Remove all documents from "ordersfrommenu"
                for (document in ordersSnapshot.documents) {
                    currentUserDocument.collection("ordersfrommenu").document(document.id).delete()
                }
            }
            .addOnFailureListener { exception ->
                // Toast for failure in fetching orders
                Toast.makeText(
                    this,
                    "Failed to fetch orders",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }

    override fun onPaymentError(paytmErrorCode: Int, errorDescription: String?, p2: PaymentData?) {
        // Handle payment failure
        Toast.makeText(this, "Payment Failed: $errorDescription", Toast.LENGTH_SHORT).show()

    }



}
