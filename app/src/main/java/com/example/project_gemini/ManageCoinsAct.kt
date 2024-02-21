package com.example.project_gemini

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_gemini.databinding.ActivityManageCoinsBinding
import com.example.project_gemini.databinding.LayoutOffersItemBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class ManageCoinsAct : AppCompatActivity() {

    private lateinit var binding: ActivityManageCoinsBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var offersAdapter: OffersAdapter
    private var contact: String? = null
    private var currentTotalCoins: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCoinsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("NAME")
        val coinEarned = intent.getStringExtra("COIN_EARNED")
        contact = intent.getStringExtra("CONTACT")

        binding.textView11.text = name
        binding.textViewUserDetails.text = coinEarned

        // Set up Firestore listener for real-time updates on users data
        val docRef = contact?.let { db.collection("users").document(it) }

        docRef?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                showToast("Error fetching data from Firebase.")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val currentTotalCoins = snapshot.getLong("CurrentTotalCoins")

                if (currentTotalCoins != null) {
                    binding.textViewUserDetails.text = "$currentTotalCoins hushh coins"
                    showToast("CurrentTotalCoins: $currentTotalCoins")
                    // Set up RecyclerView
                    offersAdapter = OffersAdapter(emptyList(), contact, currentTotalCoins)
                    binding.horizontalGridView.layoutManager = GridLayoutManager(this, 2)
                    binding.horizontalGridView.adapter = offersAdapter
                } else {
                    showToast("CurrentTotalCoins field not available.")
                }
            } else {
                showToast("Document does not exist.")
            }
        }

        // Set up Firestore listener for real-time updates on hushhOffers data
        val offersRef = db.collection("hushhOffers")

        offersRef.addSnapshotListener { documents, exception ->
            if (exception != null) {
                showToast("Error getting documents: $exception")
                return@addSnapshotListener
            }

            val offerList = mutableListOf<Offer>()

            for (document in documents!!) {
                val nameOfVoucher = document.getString("NameofVoucher")
                val aboutVoucher = document.getString("AboutVoucher")
                val voucherPoints = document.getString("VoucherPoints")
                val voucherStock = document.getString("VoucherStock")
                val voucherPrice = document.getString("VoucherPrice")
                val enable = document.getString("Enable")
                val timestamp = document.getString("timestamp")

                if (nameOfVoucher != null &&
                    aboutVoucher != null &&
                    voucherPoints != null &&
                    voucherStock != null &&
                    voucherPrice != null &&
                    enable != null &&
                    timestamp != null
                ) {
                    val offer = Offer(
                        nameOfVoucher,
                        aboutVoucher,
                        "$voucherPoints points",
                        "$voucherStock in stock",
                        voucherPrice,
                        enable,
                        timestamp
                    )
                    offerList.add(offer)
                }
            }

            // Notify the adapter of data changes
            offersAdapter.setOffers(offerList)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

data class Offer(
    val nameOfVoucher: String,
    val aboutVoucher: String,
    val voucherPoints: String,
    val voucherStock: String,
    val voucherPrice: String,
    val enable: String,
    val timestamp: String
)

class OffersAdapter(
    private var offers: List<Offer>,
    private var contact: String?,
    private var currentTotalCoins: Long?
) :
    RecyclerView.Adapter<OffersAdapter.OfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding =
            LayoutOffersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(contact, currentTotalCoins, offers[position])
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    fun setOffers(offerList: List<Offer>) {
        this.offers = offerList
        notifyDataSetChanged()
    }

    class OfferViewHolder(private val binding: LayoutOffersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: String?, currentTotalCoins: Long?, offer: Offer) {
            binding.apply {
                textView28.text = offer.voucherPoints
                textView29.text = offer.voucherStock
                textView30.text = offer.nameOfVoucher
                textView31.text = offer.aboutVoucher

                if (currentTotalCoins != null) {
                    linearLayout3.setOnClickListener {
                        if (extractNumericValue(offer.voucherStock) <= 0) {
                            showToast("Voucher is out of stock")
                        } else if (extractPoints(offer.voucherPoints) > currentTotalCoins) {
                            showToast("You don't have sufficient coins")
                        } else {
                            // Perform the Firebase Firestore operations here
                            // Update "rewards" and decrement "hushhcoins"
                            updateRewardsInFirestore(offer.nameOfVoucher, offer, contact)
                            updateHushhCoinsInFirestore(
                                offer.nameOfVoucher,
                                extractPoints(offer.voucherPoints),
                                contact
                            )
                            // Update voucherStock
                            val numericVoucherStock = extractNumericValue(offer.voucherStock)
                            updateVoucherStockInFirestore(offer.nameOfVoucher,
                                numericVoucherStock.toString()
                            )

                            showToast("Clicked on: ${offer.nameOfVoucher}")
                        }
                    }
                } else {
                    showToast("CurrentTotalCoins is null")
                }
            }
        }

        private fun extractNumericValue(input: String): Int {
            val regex = Regex("\\d+")
            val matchResult = regex.find(input)
            return matchResult?.value?.toIntOrNull() ?: 0
        }

        private fun updateRewardsInFirestore(nameOfVoucher: String, offer: Offer, contact: String?) {
            // Firestore reference for users collection
            val usersRef = FirebaseFirestore.getInstance().collection("users")

            // Generate a timestamp
            val timestamp = System.currentTimeMillis().toString()

            // Update rewards in the inner collection
            contact?.let {
                val rewardsRef = usersRef.document(it).collection("rewards").document("$nameOfVoucher$timestamp")

                val rewardsData = mapOf(
                    "nameOfVoucher" to offer.nameOfVoucher,
                    "aboutVoucher" to offer.aboutVoucher,
                    "voucherPoints" to offer.voucherPoints,
                    "voucherStock" to offer.voucherStock,
                    "voucherPrice" to offer.voucherPrice,
                    "timestamp" to offer.timestamp
                )
                rewardsRef.set(rewardsData)
                    .addOnSuccessListener {
                        showToast("Rewards updated successfully.")

                        // Open WhatsApp with the specified details
                        val phoneNumber = "+918004482372"
                        val message = "Voucher details:\n" +
                                "Order Id: ${nameOfVoucher}${timestamp}\n" +
                                "Name: ${offer.nameOfVoucher}\n" +
                                "About: ${offer.aboutVoucher}\n" +
                                "Points: ${offer.voucherPoints}\n" +
                                "Stock: ${offer.voucherStock}\n" +
                                "Timestamp: ${offer.timestamp}"

                        openWhatsApp(phoneNumber, message)
                    }
                    .addOnFailureListener {
                        showToast("Failed to update rewards.")
                    }
            }
        }

        private fun openWhatsApp(phoneNumber: String, message: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://wa.me/$phoneNumber/?text=${Uri.encode(message)}"
            intent.data = Uri.parse(url)

            try {
                binding.root.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                showToast("WhatsApp not installed on your device")
            }
        }


        private fun updateHushhCoinsInFirestore(nameOfVoucher: String, voucherPoints: Int, contact: String?) {
            // Firestore reference for users collection
            val usersRef = FirebaseFirestore.getInstance().collection("users")

            // Extract the last 6 digits from the timestamp
            val timestamp = System.currentTimeMillis().toString()
            val lastSixDigits = timestamp.takeLast(6)

            // Append the last 6 digits to the nameOfVoucher
            val updatedNameOfVoucher = "$nameOfVoucher$lastSixDigits"

            // Update hushhcoins in the inner collection
            contact?.let {
                val hushhCoinsRef = usersRef.document(it).collection("coins").document("hushhcoins")
                hushhCoinsRef.update(updatedNameOfVoucher, FieldValue.increment(-voucherPoints.toLong()))
                    .addOnSuccessListener {
                        showToast("Hushh coins updated successfully.")
                    }
                    .addOnFailureListener {
                        showToast("Failed to update hushh coins.")
                    }
            }
        }

        private fun updateVoucherStockInFirestore(nameOfVoucher: String, currentVoucherStock: String) {
            // Extract numeric value from currentVoucherStock
            val currentStockValue = extractNumericValue(currentVoucherStock)

            // Calculate new stock value after decrement
            val newStockValue = currentStockValue - 1

            // Create the updated voucher stock string
            val newVoucherStock = "$newStockValue"

            // Firestore reference for hushhOffers collection
            val hushhOffersRef = FirebaseFirestore.getInstance().collection("hushhOffers")

            // Update voucherStock in the hushhOffers collection
            val query = hushhOffersRef.whereEqualTo("NameofVoucher", nameOfVoucher)
            query.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.update("VoucherStock", newVoucherStock)
                            .addOnSuccessListener {
                                showToast("Voucher stock updated successfully.")
                            }
                            .addOnFailureListener {
                                showToast("Failed to update voucher stock.")
                            }
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to query hushhOffers collection.")
                }
        }


        private fun showToast(message: String) {
            Toast.makeText(binding.root.context, message, Toast.LENGTH_SHORT).show()
        }

        companion object {
            fun extractPoints(voucherPoints: String): Int {
                val regex = Regex("\\d+")
                val matchResult = regex.find(voucherPoints)
                return matchResult?.value?.toIntOrNull() ?: 0
            }
        }
    }
}
