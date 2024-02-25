package com.example.project_gemini

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_gemini.databinding.ActivityCheckoutMenuBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dev.shreyaspatil.easyupipayment.EasyUpiPayment
import dev.shreyaspatil.easyupipayment.listener.PaymentStatusListener
import dev.shreyaspatil.easyupipayment.model.PaymentApp
import dev.shreyaspatil.easyupipayment.model.TransactionDetails
import dev.shreyaspatil.easyupipayment.model.TransactionStatus
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class CheckoutMenuAct : AppCompatActivity(), PaymentStatusListener {

    private lateinit var binding: ActivityCheckoutMenuBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var easyUpiPayment: EasyUpiPayment
    // Declare ProgressDialog variable
    private var progressDialog: ProgressDialog? = null

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

        binding.textView7.text = "Invoice$name"

        // Setting current date and time
        val currentDateTime = SimpleDateFormat("dd MMM yyyy h:mm a", Locale.getDefault()).format(Date())
        binding.textView10.text = currentDateTime

        val amountWithoutDelivery = (totalOrderVolume - discountedCost).toFloat()

        val amount: Float
        // Check if current time is between 7:30 PM and 1:30 AM
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // Get current hour in 24-hour format

        val isLateNight = hour >= 22 || hour < 1 // Check if it's between 7:30 PM and 1:30 AM

        var deliveryCharge = 0
        if (isLateNight) {
            if (amountWithoutDelivery < 50) {
                amount = amountWithoutDelivery + 5
                deliveryCharge = 5
                Toast.makeText(this, "Delivery fee of Rs 5 is added", Toast.LENGTH_SHORT).show()
            } else {
                amount = amountWithoutDelivery + 10
                deliveryCharge = 10
                Toast.makeText(this, "Delivery fee of Rs 10 is added", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No delivery charge if it's not late night
            amount = amountWithoutDelivery
            Toast.makeText(this, "No delivery charge applied", Toast.LENGTH_SHORT).show()
        }


        binding.textView36.text = "Rs $deliveryCharge" // Set delivery charge text
        binding.textView20.text = "Rs$totalOrderVolume - Rs$discountedCost + Rs$deliveryCharge = Rs$amount" // Update amount text


        binding.textView20.text = "Rs$totalOrderVolume - Rs$discountedCost = Rs$amount"
        binding.textView22.text = "Rs$discountedCost"
        binding.textView201.text = "Payment Request of $amount"

        // Generate random characters
        fun generateRandomString(length: Int): String {
            val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { charset.random() }
                .joinToString("")
        }

        // Generate transactionId
        val orderId = "${System.currentTimeMillis().toString().substring(0, 8)}${generateRandomString(8)}"

        // Generate transactionRefId
        val currentTime = System.currentTimeMillis().toString()
        val transactionRefId = "${currentTime.substring(0, 10)}${generateRandomString(6)}"


        // Initialize EasyUpiPayment
        easyUpiPayment = EasyUpiPayment(this) {
            this.paymentApp = PaymentApp.ALL
            // Your Paytm merchant details
            //this.payeeVpa = "Vyapar.169774393413@hdfcbank"
            this.payeeVpa = "q761243897@ybl"
            this.payeeName = "DELHI CAFE DELIGHT"
            this.payeeMerchantCode = "68190203"
            this.description = "Order From Stumato"
            // Set transactionId and transactionRefId
            // Set transactionId and transactionRefId

            // Set transactionId and transactionRefId
            this.transactionId = "T$orderId"
            Toast.makeText(this@CheckoutMenuAct, "$transactionId", Toast.LENGTH_SHORT).show()
            this.transactionRefId = transactionRefId
            Toast.makeText(this@CheckoutMenuAct, "$transactionRefId", Toast.LENGTH_SHORT).show()
            this.amount = String.format(Locale.getDefault(), "%.2f", amount) // Format amount to two decimal places
        }

        // Set PaymentStatusListener
        easyUpiPayment.setPaymentStatusListener(this)

        // Handle imageView31 click
        binding.imageView31.setOnClickListener {
            // Start the payment process
            easyUpiPayment.startPayment()
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

    override fun onTransactionCompleted(transactionDetails: TransactionDetails) {
        if (transactionDetails.transactionStatus == TransactionStatus.SUCCESS) {
            // Handle transaction completion and retrieve details
            Toast.makeText(this, "Payment Done", Toast.LENGTH_SHORT).show()

            // Make imageView38 visible
            binding.imageView38.visibility = View.VISIBLE

            // Check if imageView38 is visible
            if (binding.imageView38.visibility == View.VISIBLE) {
                // Take screenshot of linear layout and upload
                takessamdupload()

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
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }



    private fun takessamdupload() {
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
                    updateFirestore(imageUrl,bitmap)
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

    override fun onTransactionCancelled() {
        // Handle transaction cancellation
        Toast.makeText(this, "Cancelled by user", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyUpiPayment.removePaymentStatusListener()
    }

    private fun updateFirestore(imageUrl: String, bitmap: Bitmap) {
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

        val isLateNight = hour >= 22 || hour < 1

        if (isLateNight) {
            deliveryCharge = if (amountWithoutDelivery < 50) {
                5
            } else {
                10
            }
        } else {
            deliveryCharge = 0
        }

        // Add delivery charge to totalAmount
        val totalAmountWithDelivery = totalAmount + deliveryCharge



        val firestore = FirebaseFirestore.getInstance()
        val contact = intent.getStringExtra("contact")

        // Format HushhDiscount
        val hushhDiscount = "Rs ${discountedCost}.00 discount"

        // Format UserPaid
        val userPaid = String.format(Locale.getDefault(), "Rs %.2f - Rs %.2f = Rs %.2f", totalAmount.toFloat(), discountedCost.toFloat(), amount)

        // Update 'users' collection
        val usersCollection = firestore.collection("users").document(contact.toString())
            .collection(brandname ?: "").document("${brandname}${timeStamp}")

        usersCollection.set(
            mapOf(
                "OrderId" to "${brandname}${timeStamp}",
                "OrderTotal" to totalAmountWithDelivery,
                "HushhDiscount" to hushhDiscount,
                "BillUrl" to imageUrl,
                "UserPaid" to userPaid,
                "PaymentConfirmation" to "yes",
                "Timestamp" to "${timeStamp}"
            )
        ).addOnSuccessListener {
            Toast.makeText(this, "Data saved to 'users' collection successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to save data to 'users' collection", Toast.LENGTH_SHORT).show()
        }

        // Update 'buisness_onboard' collection
        val businessOnboardCollection = firestore.collection("buisness_onboard")
            .document(brandname ?: "").collection("invoice").document("${brandname}${timeStamp}")

        businessOnboardCollection.set(
            mapOf(
                "OrderId" to "${brandname}${timeStamp}",
                "OrderTotal" to totalAmountWithDelivery,
                "HushhDiscount" to hushhDiscount,
                "BillUrl" to imageUrl,
                "UserPaid" to userPaid,
                "UserPhone" to contact.toString(),
                "PaymentConfirmation" to "yes",
                "Timestamp" to "${timeStamp}"
            )
        ).addOnSuccessListener {
            Toast.makeText(this, "Data saved to 'buisness_onboard' collection successfully", Toast.LENGTH_SHORT)
                .show()
            dismissLoadingDialog()
                // Call openWhatsApp() with the specified phone number and message
            //takessoflinearLayoutInvoice
                val name = intent.getStringExtra("name")
                openWhatsApp("+917276867747",
                    "Order by:\n$name\n"+
                            "OrderId: ${brandname}${timeStamp}\n" +
                            "OrderTotal: $totalAmountWithDelivery\n" +
                            "Discount: $hushhDiscount\n" +
                            "BillUrl: $imageUrl\n" +
                            "UserPaid: $userPaid\n" +
                            "UserPhone: ${contact.toString()}\n" +
                            "PaymentConfirmation: yes\n"
                           )

        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to save data to 'buisness_onboard' collection", Toast.LENGTH_SHORT)
                .show()
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

}
