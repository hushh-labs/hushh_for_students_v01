package com.example.project_gemini

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_gemini.composeact.MiniStoreAct
import com.example.project_gemini.databinding.ActivityShowHomeCardBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class ShowHomeCardAct : AppCompatActivity() {

    private lateinit var binding: ActivityShowHomeCardBinding
    private lateinit var card1: CardView
    private lateinit var card2: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var qaAdapter: QAAdapter
    private var isCard1Visible = true
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var progressDialog: ProgressDialog? = null
    private var currentFolderName: String? = null
    private var uploadedFileNames = mutableListOf<String>()
    private val uploadedFileHashes = mutableListOf<String>()

    private var rewardedAd: RewardedAd? = null
    private val TAG = "ShowHomeCardAct"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowHomeCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showCoinsAddedDialog()

        binding.imageView8.setOnClickListener {
            startActivity(Intent(this, Hushh_Home_Screen::class.java))
        }

        card1 = findViewById(R.id.card)
        card2 = findViewById(R.id.card2)

        card1.setOnClickListener {
            flipCard()
        }

        recyclerView = findViewById(R.id.recyclerviewqa)
        recyclerView.layoutManager = LinearLayoutManager(this)
        qaAdapter = QAAdapter()
        recyclerView.adapter = qaAdapter

        recyclerView.setOnTouchListener { _, event ->
            if (isCard1Visible) {
                card1.onTouchEvent(event)
            } else {
                card2.onTouchEvent(event)
            }
        }

        card2.setOnClickListener {
            flipCard()
        }

        val intent = intent
        val imageURL = intent.getStringExtra("imageURL")
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val dob = intent.getStringExtra("dob")
        val title = intent.getStringExtra("parentName")
        val contactNumber = intent.getStringExtra("globalPhoneNumber")
        val parentName = intent.getStringExtra("parentName")

        loadImage(imageURL)

        binding.textView12.text = "$name\n$email"

        binding.dialogPositiveButton1.setOnClickListener {
            val agentPhoneNumber = binding.textView121.text.toString().split("\n")[0]
            openWhatsApp(agentPhoneNumber, "Hi, there")
        }

        val age = calculateAge(dob)
        binding.textAge.text = age.toString()

        fetchDataFromFirestore(contactNumber, title)
        fetchUserDataFromAgentsCollection(parentName)
        fetchBusinessLogoFromFirestore(parentName)

        binding.btncheckoutMenu.setOnClickListener {
            loadAndShowRewardedAd {
                checkAndNavigate(title, name, contactNumber)
            }
        }

        binding.dialogNegativeButton.setOnClickListener {
            shareCardAsImage()
        }

        binding.btnUploadAssets.setOnClickListener {
            showLoadingDialog()
            createFirebaseFolder()
        }

        binding.btnViewAssets.setOnClickListener {
            if (contactNumber != null) {
                fetchCurrentFolderNameFromFirestore(contactNumber, parentName)
            }
        }

        binding.btncheckoutbills.setOnClickListener {
            val intent = Intent(this, Checkout_Bills::class.java).apply {
                putExtra("parentName", title)
                putExtra("globalPhoneNumber", contactNumber)
            }
            startActivity(intent)
        }

        loadRewardedAd()
    }

    private fun loadAndShowRewardedAd(onAdComplete: () -> Unit) {
        rewardedAd?.show(this) { rewardItem ->
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
            Toast.makeText(this, "Reward earned: $rewardAmount $rewardType", Toast.LENGTH_SHORT).show()
            onAdComplete()
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            // Toast.makeText(this, "Ad is not ready yet", Toast.LENGTH_SHORT).show() // Commented out
            onAdComplete()
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(this, "ca-app-pub-5762805546760080/5509843631", adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    this@ShowHomeCardAct.rewardedAd = rewardedAd
                    Log.i(TAG, "Rewarded Ad loaded successfully")
                    setFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    // Toast.makeText(this@ShowHomeCardAct, "Failed to load ad", Toast.LENGTH_SHORT).show() // Commented out
                }
            })
    }

    private fun setFullScreenContentCallback() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Rewarded Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad dismissed fullscreen content.")
                rewardedAd = null
                // Toast.makeText(this@ShowHomeCardAct, "Ad dismissed", Toast.LENGTH_SHORT).show() // Commented out
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.e(TAG, "Rewarded Ad failed to show fullscreen content: ${adError.message}")
                rewardedAd = null
                // Toast.makeText(this@ShowHomeCardAct, "Failed to show ad", Toast.LENGTH_SHORT).show() // Commented out
            }

            override fun onAdImpression() {
                Log.d(TAG, "Rewarded Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad showed fullscreen content.")
            }
        }
    }

    private fun checkAndNavigate(title: String?, name: String?, contactNumber: String?) {
        if (title == "OAC Canteen" || title == "Thapa mess") {
            val intent = Intent(this, MiniStoreAct::class.java).apply {
                putExtra("parentName", title)
                putExtra("contact", contactNumber)
                putExtra("name", name)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "We are accepting offline orders for this store, please check in store", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchCurrentFolderNameFromFirestore(globalPhoneNumber: String?, parentName: String?) {
        if (globalPhoneNumber != null && parentName != null) {
            val usersCollection = firestore.collection("users")
            val userDocument = usersCollection.document(globalPhoneNumber)
            val parentCollection = userDocument.collection(parentName)

            parentCollection.document("card_assets")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val currentFolderfetchedfromfirestoreName = documentSnapshot.getString("currentFolderName")
                    if (!currentFolderfetchedfromfirestoreName.isNullOrBlank()) {
                        showToast("Current Path Name: $currentFolderfetchedfromfirestoreName")
                        getDownloadLink(currentFolderfetchedfromfirestoreName)
                    } else {
                        showToast("Current Folder Name is empty")
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Failed to fetch current folder name from Firestore: ${e.message}")
                }
        } else {
            showToast("Invalid data for Firestore fetch")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchBusinessLogoFromFirestore(parentName: String?) {
        parentName?.let {
            val businessOnboardCollection = firestore.collection("buisness_onboard")
            businessOnboardCollection.document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val logoURL = documentSnapshot.getString("logoURL")
                    if (!logoURL.isNullOrBlank()) {
                        loadBusinessLogo(logoURL)
                    } else {
                        // Toast.makeText(this, "Logo URL is empty", Toast.LENGTH_SHORT).show() // Commented out
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch business logo: ${exception.message}", Toast.LENGTH_SHORT).show() // Important
                    Log.e("FetchBusinessLogo", "Failed to fetch business logo", exception)
                }
        } ?: run {
            // Toast.makeText(this, "Parent name is null", Toast.LENGTH_SHORT).show() // Commented out
            Log.e("FetchBusinessLogo", "Parent name is null")
        }
    }

    private fun loadBusinessLogo(logoURL: String) {
        Glide.with(this)
            .load(logoURL)
            .placeholder(R.drawable.logo_splash_screen)
            .into(binding.imageView172)
    }

    private fun showCoinsAddedDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = builder.create()

        val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val okButton = dialogView.findViewById<ImageButton>(R.id.dialogPositiveButton)
        val earnmore = dialogView.findViewById<ImageButton>(R.id.dialogNegativeButton)

        titleTextView.text = "Congratulations!"
        messageTextView.text = "Whoo!! 50 hushh coins added for adding this card"

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        earnmore.setOnClickListener {
            val contactNumber = intent.getStringExtra("globalPhoneNumber")
            val newCardMarketIntent = Intent(this, NewCardMarketAct::class.java)
            newCardMarketIntent.putExtra("CONTACT_NUMBER", contactNumber)
            startActivity(newCardMarketIntent)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openWhatsApp(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val url = "https://wa.me/$phoneNumber/?text=${Uri.encode(message)}"
        intent.data = Uri.parse(url)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp not installed on your device", Toast.LENGTH_SHORT).show() // Important
        }
    }

    private fun fetchUserDataFromAgentsCollection(parentName: String?) {
        parentName?.let {
            val usersCollection = firestore.collection("users_agents")
            usersCollection.get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.contains("brand") && document.getString("brand") == parentName) {
                            val agentPhoneNumber = document.getString("phoneNumber")
                            val agentEmailAddress = document.getString("emailAddress")

                            val resultText = "$agentPhoneNumber\n$agentEmailAddress"
                            binding.textView121.text = resultText
                            // Toast.makeText(this, "Data fetched successfully$resultText", Toast.LENGTH_SHORT).show() // Commented out
                            binding.dialogPositiveButton1.visibility = View.VISIBLE
                            break
                        }
                    }

                    if (binding.textView121.text.isBlank()) {
                        val message = "No matching document found in 'users_agents' collection for brand: $parentName"
                        // Toast.makeText(this, message, Toast.LENGTH_SHORT).show() // Commented out
                        Log.d("FetchUserData", message)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = "Failed to fetch data from users_agents: ${exception.message}"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show() // Important
                    Log.e("FetchUserData", errorMessage, exception)
                }
        } ?: run {
            val errorMessage = "Parent name is null"
            // Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show() // Commented out
            Log.e("FetchUserData", errorMessage)
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("hushhing...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun dismissLoadingDialog() {
        progressDialog?.dismiss()
    }

    private fun createFirebaseFolder() {
        val globalPhoneNumber = intent.getStringExtra("globalPhoneNumber")
        val parentName = intent.getStringExtra("parentName")

        val folderPath = "$globalPhoneNumber/$parentName"
        val storageRef = storage.reference.child(folderPath)

        storageRef.putBytes(byteArrayOf())
            .addOnSuccessListener {
                dismissLoadingDialog()
                Toast.makeText(this, "Folder created or already exists", Toast.LENGTH_SHORT).show() // Important
                currentFolderName = folderPath
                saveFolderInfoToFirestore(globalPhoneNumber, parentName, currentFolderName)
                pickAndUploadFiles()
            }
            .addOnFailureListener { e ->
                dismissLoadingDialog()
                Toast.makeText(this, "Failed to create folder: ${e.message}", Toast.LENGTH_SHORT).show() // Important
            }
    }

    private fun saveFolderInfoToFirestore(globalPhoneNumber: String?, parentName: String?, folderPath: String?) {
        if (globalPhoneNumber != null && parentName != null && folderPath != null) {
            val usersCollection = firestore.collection("users")
            val userDocument = usersCollection.document(globalPhoneNumber)
            val parentCollection = userDocument.collection(parentName)

            parentCollection.document("card_assets")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        parentCollection.document("card_assets")
                            .update(mapOf("currentFolderName" to folderPath))
                            .addOnSuccessListener {
                                val pathFetched = documentSnapshot.getString("currentFolderName")
                                // Toast.makeText(this, "Folder information updated in Firestore. Path Fetched: $pathFetched", Toast.LENGTH_SHORT).show() // Commented out
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update folder information in Firestore: ${e.message}", Toast.LENGTH_SHORT).show() // Important
                            }
                    } else {
                        parentCollection.document("card_assets")
                            .set(mapOf("currentFolderName" to folderPath))
                            .addOnSuccessListener {
                                val pathFetched = folderPath
                                // Toast.makeText(this, "Folder information saved to Firestore. Path Fetched: $pathFetched", Toast.LENGTH_SHORT).show() // Commented out
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save folder information to Firestore: ${e.message}", Toast.LENGTH_SHORT).show() // Important
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to check if 'card_assets' document exists: ${e.message}", Toast.LENGTH_SHORT).show() // Important
                }
        } else {
            // Toast.makeText(this, "Invalid data for Firestore update", Toast.LENGTH_SHORT).show() // Commented out
        }
    }

    private fun pickAndUploadFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { fileUri ->
                uploadFileToFirebaseStorage(fileUri, currentFolderName)
            }
        }
    }

    private fun calculateHash(inputStream: InputStream): String {
        val md = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            md.update(buffer, 0, bytesRead)
        }
        val digest = md.digest()
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun uploadFileToFirebaseStorage(fileUri: Uri, folderName: String?) {
        folderName?.let { folder ->
            showLoadingDialog()

            val inputStream = contentResolver.openInputStream(fileUri)
            val fileHash = inputStream?.let { calculateHash(it) }

            if (uploadedFileHashes.contains(fileHash)) {
                dismissLoadingDialog()
                showToast("You already have this file in the folder") // Important
            } else {
                val storageRef = storage.reference.child("$folder/$fileHash")

                storageRef.putFile(fileUri)
                    .addOnSuccessListener {
                        storageRef.parent?.listAll()
                            ?.addOnSuccessListener { listResult ->
                                val totalItems = listResult.items.size
                                if (totalItems >= 10) {
                                    updateCoinsInFirestore(
                                        intent.getStringExtra("globalPhoneNumber"),
                                        folder,
                                        totalItems
                                    )
                                }
                                showToast("File uploaded successfully\nTotal items in folder: $totalItems") // Important
                            }
                            ?.addOnFailureListener { e ->
                                showToast("Failed to get the total number of items in the folder: ${e.message}") // Important
                            }

                        dismissLoadingDialog()
                        fileHash?.let { uploadedFileHashes.add(it) }
                    }
                    .addOnFailureListener { e ->
                        dismissLoadingDialog()
                        showToast("Failed to upload file: ${e.message}") // Important
                    }
            }
        }
    }

    private fun updateCoinsInFirestore(contactNumber: String?, folder: String?, totalItems: Int) {
        contactNumber?.let { phoneNumber ->
            folder?.let { folderName ->
                val usersCollection = firestore.collection("users")
                val userDocument = usersCollection.document(phoneNumber)
                val coinsCollection = userDocument.collection("coins")
                val hushhCoinsDocument = coinsCollection.document("hushhcoins")
                val fieldName = FieldPath.of("${folder}dataupload")

                val maxValue = 100

                hushhCoinsDocument.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        val newValue = totalItems * 20
                        val limitedValue = newValue.coerceAtMost(maxValue)
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            hushhCoinsDocument
                                .update(fieldName, limitedValue)
                                .addOnSuccessListener {
                                    showToast("Coins updated successfully") // Important
                                }
                                .addOnFailureListener { e ->
                                    showToast("Failed to update coins: ${e.message}") // Important
                                }
                        } else {
                            val data = hashMapOf(fieldName to limitedValue)
                            hushhCoinsDocument
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener {
                                    showToast("Coins field created and updated successfully") // Important
                                }
                                .addOnFailureListener { e ->
                                    showToast("Failed to create and update coins field: ${e.message}") // Important
                                }
                        }
                    } else {
                        showToast("Failed to check if the aggregation field exists: ${task.exception?.message}") // Important
                    }
                }
            } ?: run {
                showToast("Invalid folder name for Firestore update") // Important
            }
        } ?: run {
            showToast("Invalid contact number for Firestore update") // Important
        }
    }

    private fun shareCardAsImage() {
        val cardBitmap = getBitmapFromView(card1)
        val file = saveBitmapToFile(cardBitmap)
        val contentUri = FileProvider.getUriForFile(this, "com.example.project_gemini.fileprovider", file)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Card"))
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "card_image_$timeStamp.jpg"
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(directory, fileName)

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    private fun fetchDataFromFirestore(contactNumber: String?, title: String?) {
        contactNumber?.let {
            firestore.collection("users")
                .document(it)
                .collection(title ?: "")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val qaList = mutableListOf<QAItem>()
                    for (document in querySnapshot.documents) {
                        val question = document.getString("question") ?: ""
                        val answer = document.getString("answer") ?: ""
                        qaList.add(QAItem(question, answer))
                    }
                    qaAdapter.submitList(qaList)
                }
                .addOnFailureListener { e ->
                    showToast("Failed to fetch data: ${e.message}") // Important
                }
        }
    }

    private fun flipCard() {
        val visibleCard: View
        val invisibleCard: View

        if (isCard1Visible) {
            visibleCard = card1
            invisibleCard = card2
        } else {
            visibleCard = card2
            invisibleCard = card1
        }

        val animatorOut = ObjectAnimator.ofFloat(visibleCard, "rotationY", 0f, 90f)
        animatorOut.duration = 120

        val animatorIn = ObjectAnimator.ofFloat(invisibleCard, "rotationY", -90f, 0f)
        animatorIn.duration = 120

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

    private fun loadImage(imageURL: String?) {
        Glide.with(this)
            .load(imageURL)
            .placeholder(R.drawable.logo_splash_screen)
            .into(binding.cardImage)
    }

    private fun calculateAge(dob: String?): Int {
        dob?.let {
            val dobDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
            dobDate?.let { date ->
                val currentDate = Date()
                val ageInMillis = currentDate.time - date.time
                return (ageInMillis / (365.25 * 24 * 60 * 60 * 1000)).toInt()
            }
        }
        return 0
    }

    private fun getDownloadLink(folderPath: String) {
        if (folderPath.isNotBlank()) {
            val storageRef = storage.reference.child(folderPath)
            storageRef.listAll()
                .addOnSuccessListener { listResult ->
                    val downloadUrls = mutableListOf<String>()
                    listResult.items.forEach { item ->
                        item.downloadUrl
                            .addOnSuccessListener { uri ->
                                downloadUrls.add(uri.toString())
                                if (downloadUrls.size == listResult.items.size) {
                                    val concatenatedUrls = downloadUrls.joinToString("  +hushh+  ")
                                    setQrInDataCard(concatenatedUrls)
                                }
                            }
                            .addOnFailureListener { e ->
                                showToast("Failed to get download link for ${item.name}: ${e.message}") // Important
                            }
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Failed to list items in the folder: ${e.message}") // Important
                }
        } else {
            showToast("Invalid folder path") // Important
        }
    }

    private fun shareDataCardAsImage(qrCodeContent: String) {
        val cardBitmap = getBitmapFromView(binding.cardShareInfo)
        val file = saveBitmapToFile(cardBitmap)
        val contentUri = FileProvider.getUriForFile(this, "com.example.project_gemini.fileprovider", file)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Card"))
    }

    private fun setQrInDataCard(qrCodeContent: String) {
        val qrCodeBitmap = generateQrCodeBitmap(qrCodeContent)
        binding.imageView174.setImageBitmap(qrCodeBitmap)
        shareDataCardAsImage(qrCodeContent)
    }

    private fun generateQrCodeBitmap(content: String): Bitmap? {
        return try {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun shareFolderLinks(links: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, links)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Share Folder Links"))
    }

    companion object {
        private const val PICK_FILE_REQUEST_CODE = 123
    }
}
