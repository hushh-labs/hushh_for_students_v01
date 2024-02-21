package com.example.project_gemini

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.project_gemini.model.UserModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview
import com.journeyapps.barcodescanner.BarcodeEncoder

class Hushh_Home_Screen : AppCompatActivity() {

    private lateinit var card1: CardView
    private lateinit var card2: CardView

    private lateinit var textViewName: TextView
    private lateinit var textViewCardName: TextView
    private lateinit var textViewContact: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewDob: TextView
    private lateinit var imageViewQR: ImageView
    private lateinit var fabButton: FloatingActionButton
    private var globalPhoneNumber: String? = null
    private lateinit var textCoinEarned: TextView

    private lateinit var databaseReference: DatabaseReference

    private var isCard1Visible = true

    private val firestore = FirebaseFirestore.getInstance()



    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var cardRecycler: RecyclerView
    private lateinit var cardRecyclerAdapter: CardRecyclerAdapter
    private val cardItemList = mutableListOf<CustomCardItem>()

    private lateinit var lottieAnimationView: LottieAnimationView

    // Define a list of card names

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, Register_Activity::class.java))
            finish()
            return
        }



        setContentView(R.layout.activity_hushh_home_screen)




        textViewContact = findViewById(R.id.textViewContact)
        textCoinEarned = findViewById(R.id.textcoinearned)
        lottieAnimationView = findViewById(R.id.surprise)

        if (isBiometricAuthAvailable()) {
            // Biometric authentication is available, proceed with authentication
            authenticateWithBiometric()
        } else {
            // Biometric authentication is not available on this device
            showToast("Biometric authentication is not available on this device.")
        }

        globalPhoneNumber = textViewContact.text.toString()

        val phoneNumberExtra = intent.getStringExtra("PHONE_NUMBER")
        if (!phoneNumberExtra.isNullOrEmpty()) {
            createHushhCoinsDocument(phoneNumberExtra)
        } else {
            // Handle the case when phoneNumberExtra is null or empty
        }

        if (!phoneNumberExtra.isNullOrEmpty()) {
            textViewContact.text = phoneNumberExtra
            retrieveAdditionalDataFromFirestore(phoneNumberExtra)
        } else {
            val sharedPreferences = getSharedPreferences("USER_DATA", MODE_PRIVATE)
            val storedPhoneNumber = sharedPreferences.getString("PHONE_NUMBER", null)

            if (!storedPhoneNumber.isNullOrEmpty()) {
                textViewContact.text = storedPhoneNumber
                addCoinEarnedFieldIfNotExists(storedPhoneNumber)
                retrieveAdditionalDataFromFirestore(storedPhoneNumber)
            }
        }



        val imageView3: ImageView = findViewById(R.id.imageView3)

        imageView3.setOnClickListener {
            val phoneNumberExtra = textViewContact.text.toString()

            if (phoneNumberExtra.isNotEmpty()) {
                val intent = Intent(this, NewCardMarketAct::class.java)
                intent.putExtra("CONTACT_NUMBER", phoneNumberExtra)
                startActivity(intent)
            } else {
                showToast("Contact number is empty.")
            }
        }

        card1 = findViewById(R.id.card)
        card2 = findViewById(R.id.card2)

        textViewCardName = findViewById(R.id.textView)
        textViewName = findViewById(R.id.textViewName)
        textViewContact = findViewById(R.id.textViewContact)
        textViewEmail = findViewById(R.id.textViewemail)
        textViewDob = findViewById(R.id.textViewDob)
        imageViewQR = findViewById(R.id.imageView9)

        // Inside the onCreate method of Hushh_Home_Screen activity
        val imageView2: ImageView = findViewById(R.id.imageView2)

        imageView2.setOnClickListener {
            val intent = Intent(this, HushhCoinsEarnedAct::class.java)
            intent.putExtra("COIN_EARNED", textCoinEarned.text.toString())
            intent.putExtra("CONTACT", textViewContact.text.toString())
            intent.putExtra("NAME", textViewName.text.toString())
            startActivity(intent)
        }


        card1.setOnClickListener {
            flipCard()
        }

        card2.setOnClickListener {
            flipCard()
        }




        cardRecycler = findViewById(R.id.cardrecycler)
        cardRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Inside the onCreate method
        cardRecyclerAdapter = CardRecyclerAdapter(cardItemList) { parentName ->
            showToast("Clicked on item from $parentName")

            // Get the additional data
            val name = textViewName.text.toString()
            val contact = textViewContact.text.toString()
            val email = textViewEmail.text.toString()
            val dob = textViewDob.text.toString()

            // Redirect the user to ShowHomeCardAct with intent or shared preference
            val intent = Intent(this, ShowHomeCardAct::class.java)
            intent.putExtra("imageURL", getImageUrlForParentName(parentName))
            intent.putExtra("parentName", parentName)
            intent.putExtra("globalPhoneNumber", globalPhoneNumber)

            // Pass additional data
            intent.putExtra("name", name)
            intent.putExtra("contact", contact)
            intent.putExtra("email", email)
            intent.putExtra("dob", dob)

            startActivity(intent)
        }


        val carouselRecyclerview = findViewById<CarouselRecyclerview>(R.id.cardrecycler)
        carouselRecyclerview.adapter = cardRecyclerAdapter




        // Set carousel properties
        carouselRecyclerview.apply {
            set3DItem(true)
            setAlpha(true)
            setInfinite(true)
        }

        fetchCardImagesFromFirebase(globalPhoneNumber)

    }

    private fun createHushhCoinsDocument(phoneNumber: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the hushhcoins collection
        val hushhCoinsCollectionRef = firestore.collection("users")
            .document(phoneNumber)
            .collection("coins")

        // Check if the hushhcoins document already exists
        hushhCoinsCollectionRef.document("hushhcoins")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    // If not, create the hushhcoins document with an initial value of 0
                    hushhCoinsCollectionRef.document("hushhcoins")
                        .set(mapOf("hushh_signup" to 500))
                        .addOnSuccessListener {
                            showToast("Added 'hushh_signup' field with value 500.")
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to add 'hushh_signup' field: ${e.message}")
                        }
                } else {
                    showToast("hushhcoins document already exists.")
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to check for hushhcoins document: ${e.message}")
            }
    }


    private fun addCoinEarnedFieldIfNotExists(phoneNumber: String) {
        val usersCollection = firestore.collection("users")
        val userDocumentRef = usersCollection.document(phoneNumber)
        val coinsCollection = userDocumentRef.collection("coins")

        // Create "coins" subcollection if it doesn't exist
        coinsCollection.addSnapshotListener { coinsCollectionSnapshot, _ ->
            if (coinsCollectionSnapshot?.documents.isNullOrEmpty()) {
                coinsCollection.document("hushhcoins")
                    .set(mapOf("hushh_signup" to 500))
                    .addOnSuccessListener {
                        showToast("Added 'hushhcoins' subcollection with 'hushh_signup' field.")
                    }
                    .addOnFailureListener { e ->

                    }
            } else {
                // "coins" subcollection exists, check if "hushhcoins" document exists
                val hushhCoinsDocumentRef = coinsCollection.document("hushhcoins")

                hushhCoinsDocumentRef.get()
                    .addOnSuccessListener { hushhCoinsDocumentSnapshot ->
                        if (!hushhCoinsDocumentSnapshot.exists()) {
                            // "hushhcoins" document does not exist, create it
                            hushhCoinsDocumentRef
                                .set(mapOf("hushh_signup" to 0))
                                .addOnSuccessListener {
                                    showToast("Added 'hushhcoins' document with 'hushh_signup' field.")
                                }
                                .addOnFailureListener { e ->
                                    showToast("Failed to add 'hushhcoins' document: ${e.message}")
                                }
                        }
                    }
            }
        }
    }




    // ... (other methods)


    @RequiresApi(Build.VERSION_CODES.P)
    private fun authenticateWithBiometric() {
        val biometricPrompt = BiometricPrompt.Builder(this)
            .setTitle("hushh wallet Authentication")
            .setSubtitle("Use your fingerprint to log in")
            .setNegativeButton("Cancel", mainExecutor, DialogInterface.OnClickListener { _, _ ->
                notifyUser("Authentication Cancelled")
            }).build()

        biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
    }


    private fun getCancellationSignal(): CancellationSignal {
        return CancellationSignal()
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication Error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication Succeeded")

                    // Check if "hushh_signup" is not equal to zero
                    val phoneNumber = textViewContact.text.toString()
                    val coinsCollection = firestore.collection("users").document(phoneNumber).collection("coins")

                    coinsCollection.document("hushhcoins")
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val hushhSignupCoins = documentSnapshot.getLong("hushh_signup") ?: 0

                            // Skip animation if "hushh_signup" is not equal to zero
                            if (hushhSignupCoins != 0L) {
                                checkAndUpdateFirestore()
                            } else {
                                // Start Lottie animation when biometric authentication starts
                                lottieAnimationView.playAnimation()
                                lottieAnimationView.visibility = View.VISIBLE
                                checkAndUpdateFirestore()

                                // Delay for 2 seconds before checking and updating Firestore
                                Handler().postDelayed({
                                    lottieAnimationView.cancelAnimation()
                                    lottieAnimationView.visibility = View.GONE
                                }, 4000)
                            }
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to retrieve user data: ${e.message}")
                        }
                }
            }

    private fun checkAndUpdateFirestore() {
        val phoneNumber = textViewContact.text.toString()
        val coinsCollection = firestore.collection("users").document(phoneNumber).collection("coins")

        coinsCollection.document("hushhcoins")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val hushhSignupCoins = documentSnapshot.getLong("hushh_signup") ?: 0

                // Check conditions and update Firestore
                if (hushhSignupCoins >= 0 && hushhSignupCoins < 500) {
                    coinsCollection.document("hushhcoins")
                        .update("hushh_signup", 500)
                        .addOnSuccessListener {
                            showToast("Updated 'hushh_signup' field with value 500.")
                        }
                        .addOnFailureListener { e ->

                        }
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to retrieve user data: ${e.message}")
            }
    }




    private fun isBiometricAuthAvailable(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure &&
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.USE_BIOMETRIC
                ) == PackageManager.PERMISSION_GRANTED &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }


    private fun getImageUrlForParentName(parentName: String): String? {
        // Add logic to get the imageURL for the given parentName from cardItemList
        val matchingItem = cardItemList.find { it.parentName == parentName }
        return matchingItem?.imageUrl
    }

    private fun fetchCardImagesFromFirebase(contactNumber: String?) {
        if (contactNumber.isNullOrEmpty()) {
            showToast("Contact number is null or empty.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        val cardsReference = databaseReference.child("users").child(contactNumber)

        cardsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItemList.clear()

                for (cardSnapshot in snapshot.children) {
                    val parentName = cardSnapshot.key // Get the parent node name
                    val imageUrl = cardSnapshot.child("imageURL").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        cardItemList.add(CustomCardItem(imageUrl, parentName!!))
                    }
                }

                // Notify the adapter that the data set has changed
                cardRecyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to retrieve card images: ${error.message}")
            }
        })
    }




    private fun flipCard() {
        val visibleCard: View
        val invisibleCard: View

        if (isCard1Visible) {
            visibleCard = card1
            invisibleCard = card2

            findViewById<View>(R.id.cardrecycler).visibility = View.GONE
            findViewById<View>(R.id.labelbrandcard).visibility = View.GONE

        } else {
            visibleCard = card2
            invisibleCard = card1

            findViewById<View>(R.id.cardrecycler).visibility = View.VISIBLE
            findViewById<View>(R.id.labelbrandcard).visibility = View.VISIBLE
        }

        val animatorOut = ObjectAnimator.ofFloat(visibleCard, "rotationY", 0f, 90f)
        animatorOut.duration = 120
        animatorOut.interpolator = AccelerateDecelerateInterpolator()

        val animatorIn = ObjectAnimator.ofFloat(invisibleCard, "rotationY", -90f, 0f)
        animatorIn.duration = 120
        animatorIn.interpolator = AccelerateDecelerateInterpolator()

        animatorOut.start()
        animatorOut.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                visibleCard.visibility = View.GONE
                invisibleCard.visibility = View.VISIBLE
                animatorIn.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        isCard1Visible = !isCard1Visible
    }

    // Inside the retrieveAdditionalDataFromFirestore function

    private fun retrieveAdditionalDataFromFirestore(phoneNumber: String) {
        showToast("Phone Number: $phoneNumber")

        globalPhoneNumber = phoneNumber

        // Reference to the hushhcoins collection
        val hushhCoinsCollectionRef = firestore.collection("users")
            .document(phoneNumber)
            .collection("coins")

        // Add a real-time listener to hushhcoins collection
        hushhCoinsCollectionRef.addSnapshotListener { querySnapshot, error ->
            if (error != null) {

                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                var totalCoins: Long = 0L

                // Iterate through the documents
                for (document in querySnapshot.documents) {
                    // Iterate through the fields inside each document
                    for (field in document.data.orEmpty()) {
                        val coinsValue = (field.value as? Long) ?: 0
                        totalCoins += coinsValue
                    }
                }



                // Update the TextView with the total sum
                textCoinEarned.text = totalCoins.toString()

                // Save the totalCoins value in Firestore under "CurrentTotalCoins" field
                saveTotalCoinsToFirestore(phoneNumber, totalCoins)


                // Continue with fetching other user data if needed
                firestore.collection("users")
                    .document(phoneNumber)
                    .get()
                    .addOnSuccessListener { documentSnapshotUser ->
                        if (documentSnapshotUser.exists()) {
                            val additionalData = documentSnapshotUser.toObject(UserModel::class.java)

                            textViewName.text = "${additionalData?.firstName} ${additionalData?.lastName}"
                            textViewCardName.text = "${additionalData?.firstName} ${additionalData?.lastName}"
                            textViewContact.text = additionalData?.phoneNumber ?: ""
                            textViewEmail.text = additionalData?.emailAddress
                            textViewDob.text = additionalData?.birthday

                            generateQRCode()
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            } else {

            }
        }
    }

    private fun saveTotalCoinsToFirestore(phoneNumber: String, totalCoins: Long) {
        val usersCollection = firestore.collection("users")
        val userDocumentRef = usersCollection.document(phoneNumber)

        // Update the "CurrentTotalCoins" field with the new totalCoins value
        userDocumentRef
            .update("CurrentTotalCoins", totalCoins)
            .addOnSuccessListener {
                showToast("Updated 'CurrentTotalCoins' field with value $totalCoins.")
            }
            .addOnFailureListener { e ->
                showToast("Failed to update 'CurrentTotalCoins' field: ${e.message}")
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun generateQRCode() {
        val webAppUrl = "https://hush8-pay.web.app/"
        val qrCodeData = webAppUrl +
                "?firstName=${textViewName.text.toString()}" +
                "&lastName=" +
                "&phoneNumber=${textViewContact.text.toString()}" +
                "&emailAddress=${textViewEmail.text.toString()}"
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 500, 500)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imageViewQR.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}