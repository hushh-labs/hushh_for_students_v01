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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.util.Log
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
import com.example.project_gemini.composeact.HushhJobAct
import com.example.project_gemini.model.UserModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, Register_Activity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_hushh_home_screen)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val installedVersionCode = packageInfo.versionCode.toString()

        Log.d("VersionCheck", "Installed Version Code: $installedVersionCode")

        Toast.makeText(this, "Installed Version Code: $installedVersionCode", Toast.LENGTH_SHORT).show()

        val versionUpdateRef = firestore.collection("version_update ").document("versionCodehfs")
        versionUpdateRef.addSnapshotListener { snapshot: DocumentSnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                Log.e("VersionCheck", "Error fetching Firestore version: ${error.message}")
                Toast.makeText(this, "Error fetching Firestore version: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.d("VersionCheck", "Snapshot is not null")
                if (snapshot.exists()) {
                    val firestoreVersionCode = snapshot.getString("versionCode") ?: ""
                    Log.d("VersionCheck", "Firestore Version Code Retrieved: $firestoreVersionCode")
                    Toast.makeText(this, "Firestore Version Code: $firestoreVersionCode", Toast.LENGTH_SHORT).show()

                    if (firestoreVersionCode != installedVersionCode) {
                        Log.d("VersionCheck", "Version mismatch: Firestore version is $firestoreVersionCode, installed version is $installedVersionCode")
                        Toast.makeText(this, "Version mismatch: Updating app...", Toast.LENGTH_SHORT).show()
                        retrieveUpdateLink()
                    } else {
                        Log.d("VersionCheck", "App is up-to-date.")
                        Toast.makeText(this, "App is up-to-date.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("VersionCheck", "Snapshot exists check failed")
                    Toast.makeText(this, "No version info found in Firestore.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("VersionCheck", "Snapshot is null")
                Toast.makeText(this, "No version info found in Firestore.", Toast.LENGTH_SHORT).show()
            }
        }

        textViewContact = findViewById(R.id.textView39)
        textCoinEarned = findViewById(R.id.textcoinearned)

        if (isBiometricAuthAvailable()) {
            authenticateWithBiometric()
        } else {
            showToast("Biometric authentication is not available on this device.")
        }

        val imageView46: ImageView = findViewById(R.id.imageView46)
        imageView46.setOnClickListener {
            val intent = Intent(this, HushhJobAct::class.java)
            startActivity(intent)
        }

        globalPhoneNumber = textViewContact.text.toString()
        val phoneNumberExtra = intent.getStringExtra("PHONE_NUMBER")

        if (!phoneNumberExtra.isNullOrEmpty()) {
            createHushhCoinsDocument(phoneNumberExtra)
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

        textViewCardName = findViewById(R.id.textView)
        textViewName = findViewById(R.id.textViewName)
        textViewContact = findViewById(R.id.textViewContact)
        textViewEmail = findViewById(R.id.textViewemail)
        textViewDob = findViewById(R.id.textViewDob)
        imageViewQR = findViewById(R.id.imageView9)

        val imageView39: ImageView = findViewById(R.id.imageView39)
        imageView39.setOnClickListener {
            val intent = Intent(this, HushhCoinsEarnedAct::class.java)
            intent.putExtra("COIN_EARNED", textCoinEarned.text.toString())
            intent.putExtra("CONTACT", textViewContact.text.toString())
            intent.putExtra("NAME", textViewName.text.toString())
            startActivity(intent)
        }

        val imageView45: ImageView = findViewById(R.id.imageView45)
        imageView45.setOnClickListener {
            showToast("You are already on the home screen")
        }

        cardRecycler = findViewById(R.id.cardrecycler)
        cardRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        cardRecyclerAdapter = CardRecyclerAdapter(cardItemList) { parentName ->
            showToast("Clicked on item from $parentName")
            val name = textViewName.text.toString()
            val contact = textViewContact.text.toString()
            val email = textViewEmail.text.toString()
            val dob = textViewDob.text.toString()

            val intent = Intent(this, ShowHomeCardAct::class.java)
            intent.putExtra("imageURL", getImageUrlForParentName(parentName))
            intent.putExtra("parentName", parentName)
            intent.putExtra("globalPhoneNumber", globalPhoneNumber)
            intent.putExtra("name", name)
            intent.putExtra("contact", contact)
            intent.putExtra("email", email)
            intent.putExtra("dob", dob)

            startActivity(intent)
        }

        val carouselRecyclerview = findViewById<CarouselRecyclerview>(R.id.cardrecycler)
        carouselRecyclerview.adapter = cardRecyclerAdapter

        carouselRecyclerview.apply {
            set3DItem(true)
            setAlpha(true)
            setInfinite(true)
            setIntervalRatio(0.56f)
        }

        fetchCardImagesFromFirebase(globalPhoneNumber)
    }

    private fun retrieveUpdateLink() {
        val updateLinkRef = firestore.collection("version_update ").document("apkupdatedlink")
        updateLinkRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val updateLink = documentSnapshot.getString("link")
                    openUpdateLink(updateLink)
                } else {
                    Log.e("VersionCheck", "Update link document does not exist.")
                    Toast.makeText(this, "Update link document does not exist.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to retrieve update link: ${e.message}")
            }
    }

    private fun openUpdateLink(link: String?) {
        if (link != null && link.isNotEmpty()) {
            Log.d("VersionCheck", "Opening update link: $link")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
            finish()
        } else {
            Log.e("VersionCheck", "Update link is empty or null.")
            Toast.makeText(this, "Update link is empty.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE_APP) {
            if (resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }

    private fun createHushhCoinsDocument(phoneNumber: String) {
        val firestore = FirebaseFirestore.getInstance()
        val hushhCoinsCollectionRef = firestore.collection("users")
            .document(phoneNumber)
            .collection("coins")

        hushhCoinsCollectionRef.document("hushhcoins")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
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

        coinsCollection.addSnapshotListener { coinsCollectionSnapshot, _ ->
            if (coinsCollectionSnapshot?.documents.isNullOrEmpty()) {
                coinsCollection.document("hushhcoins")
                    .set(mapOf("hushh_signup" to 500))
                    .addOnSuccessListener {
                        showToast("Added 'hushhcoins' subcollection with 'hushh_signup' field.")
                    }
                    .addOnFailureListener { e ->
                        // Commented out to remove non-critical Toast message
                        // showToast("Failed to add 'hushhcoins' subcollection: ${e.message}")
                    }
            } else {
                val hushhCoinsDocumentRef = coinsCollection.document("hushhcoins")
                hushhCoinsDocumentRef.get()
                    .addOnSuccessListener { hushhCoinsDocumentSnapshot ->
                        if (!hushhCoinsDocumentSnapshot.exists()) {
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

                    val phoneNumber = textViewContact.text.toString()
                    val coinsCollection = firestore.collection("users").document(phoneNumber).collection("coins")

                    coinsCollection.document("hushhcoins")
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val hushhSignupCoins = documentSnapshot.getLong("hushh_signup") ?: 0
                            if (hushhSignupCoins != 0L) {
                                checkAndUpdateFirestore()
                            } else {
                                lottieAnimationView.playAnimation()
                                lottieAnimationView.visibility = View.VISIBLE
                                checkAndUpdateFirestore()
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
                if (hushhSignupCoins >= 0 && hushhSignupCoins < 500) {
                    coinsCollection.document("hushhcoins")
                        .update("hushh_signup", 500)
                        .addOnSuccessListener {
                            showToast("Updated 'hushh_signup' field with value 500.")
                        }
                        .addOnFailureListener { e ->
                            // Commented out to remove non-critical Toast message
                            // showToast("Failed to update 'hushh_signup' field: ${e.message}")
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

        // Use addValueEventListener to listen for real-time updates
        cardsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItemList.clear()
                for (cardSnapshot in snapshot.children) {
                    val parentName = cardSnapshot.key
                    val imageUrl = cardSnapshot.child("imageURL").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        cardItemList.add(CustomCardItem(imageUrl, parentName!!))
                    }
                }
                cardRecyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to retrieve card images: ${error.message}")
            }
        })
    }

    private fun retrieveAdditionalDataFromFirestore(phoneNumber: String) {
        showToast("Phone Number: $phoneNumber")

        globalPhoneNumber = phoneNumber
        val hushhCoinsCollectionRef = firestore.collection("users")
            .document(phoneNumber)
            .collection("coins")

        hushhCoinsCollectionRef.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                var totalCoins: Long = 0L
                for (document in querySnapshot.documents) {
                    for (field in document.data.orEmpty()) {
                        val coinsValue = (field.value as? Long) ?: 0
                        totalCoins += coinsValue
                    }
                }

                textCoinEarned.text = totalCoins.toString()
                saveTotalCoinsToFirestore(phoneNumber, totalCoins)

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
            }
        }
    }

    private fun saveTotalCoinsToFirestore(phoneNumber: String, totalCoins: Long) {
        val usersCollection = firestore.collection("users")
        val userDocumentRef = usersCollection.document(phoneNumber)

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

    companion object {
        private const val REQUEST_CODE_UPDATE_APP = 1001
    }

}
