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
import android.widget.Button
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
import com.google.android.gms.drive.Contents
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowHomeCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showCoinsAddedDialog()

        binding.imageView8.setOnClickListener {
            val intent = Intent(this, Hushh_Home_Screen::class.java)
            startActivity(intent)
        }

        card1 = findViewById(R.id.card)
        card2 = findViewById(R.id.card2)

        // Set click listeners for card flipping
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

        // Retrieve data from intent
        val intent = intent
        val imageURL = intent.getStringExtra("imageURL")
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val dob = intent.getStringExtra("dob")
        val title = intent.getStringExtra("parentName")
        val contactNumber = intent.getStringExtra("globalPhoneNumber")
        val parentName = intent.getStringExtra("parentName")


        // Load imageURL into ImageView using Glide
        loadImage(imageURL)

        // Set name and email into TextView
        binding.textView12.text = "$name\n$email"

        

        binding.dialogPositiveButton1.setOnClickListener {
            // Retrieve the agent's phone number from the TextView
            val agentPhoneNumber = binding.textView121.text.toString().split("\n")[0]

            // Create a message to be sent
            val message = "Hi, there"

            // Open WhatsApp with the agent's phone number and the message
            openWhatsApp(agentPhoneNumber, message)
        }


        // Calculate and set age into TextView
        val age = calculateAge(dob)
        binding.textAge.text = age.toString()

        fetchDataFromFirestore(contactNumber, title)

        fetchUserDataFromAgentsCollection(parentName)

        fetchBusinessLogoFromFirestore(parentName)

        binding.btncheckoutMenu.setOnClickListener {
            val title = intent.getStringExtra("parentName")
            val name = intent.getStringExtra("name")
            val contactNumber = intent.getStringExtra("globalPhoneNumber")
            
            checkAndNavigate(title, name, contactNumber)
        }



        binding.dialogNegativeButton.setOnClickListener {
            shareCardAsImage()
        }

        binding.btnUploadAssets.setOnClickListener {
            showLoadingDialog()
            createFirebaseFolder()
            // Add code here to open file picker or any mechanism for uploading images, files, etc.
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
    }

    private fun checkAndNavigate(title: String?, name: String?, contactNumber: String?) {
        if (title == "OAC Canteen") {
            // If title is "OAC Canteen", navigate to MiniStoreAct with intent
            val intent = Intent(this, MiniStoreAct::class.java).apply {
                putExtra("parentName", title)
                putExtra("contact", contactNumber)
                putExtra("name", name)
            }
            startActivity(intent)
        } else {
            // Show toast for offline orders
            Toast.makeText(this, "We are accepting offline orders for this store, please check in store", Toast.LENGTH_SHORT).show()
        }
    }



    private fun fetchCurrentFolderNameFromFirestore(globalPhoneNumber: String?, parentName: String?) {
        // Check if any of the necessary information is null
        if (globalPhoneNumber != null && parentName != null) {
            // Access Firestore collection and document
            val usersCollection = firestore.collection("users")
            val userDocument = usersCollection.document(globalPhoneNumber)

            // Inner collection for the parentName
            val parentCollection = userDocument.collection(parentName)

            // Fetch the "card_assets" document
            parentCollection.document("card_assets")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // If the document exists, retrieve the currentFolderName field
                        val currentFolderfetchedfromfirestoreName = documentSnapshot.getString("currentFolderName")
                        if (!currentFolderfetchedfromfirestoreName.isNullOrBlank()) {
                            // Display the current folder name using a Toast
                            showToast("Current Path Name: $currentFolderfetchedfromfirestoreName")
                            getDownloadLink(currentFolderfetchedfromfirestoreName)
                        } else {
                            showToast("Current Folder Name is empty")
                        }
                    } else {
                        showToast("Document 'card_assets' not found in Firestore for parentName: $parentName")
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
            // Access Firestore collection and retrieve business logo URL
            val businessOnboardCollection = firestore.collection("buisness_onboard")
            businessOnboardCollection.document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Check if the document has a "logoURL" field
                        val logoURL = documentSnapshot.getString("logoURL")
                        if (!logoURL.isNullOrBlank()) {
                            // Load logoURL into ImageView using Glide
                            loadBusinessLogo(logoURL)
                        } else {
                            Toast.makeText(this, "Logo URL is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Document not found in 'buisness_onboard' collection for parentName: $parentName", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Toast.makeText(this, "Failed to fetch business logo: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FetchBusinessLogo", "Failed to fetch business logo", exception)
                }
        } ?: run {
            // Handle the case where parentName is null
            val errorMessage = "Parent name is null"
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            Log.e("FetchBusinessLogo", errorMessage)
        }
    }

    private fun loadBusinessLogo(logoURL: String) {
        // Load business logo into ImageView using Glide
        Glide.with(this)
            .load(logoURL)
            .placeholder(R.drawable.logo_splash_screen) // Placeholder image while loading
            .into(binding.imageView172)
    }


    private fun showCoinsAddedDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = builder.create()

        // Access views in the custom layout
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val okButton = dialogView.findViewById<ImageButton>(R.id.dialogPositiveButton)
        val earnmore = dialogView.findViewById<ImageButton>(R.id.dialogNegativeButton)

        // Set values dynamically
        titleTextView.text = "Congratulations!"
        messageTextView.text = "Whoo!! 50 hushh coins added for adding this card"

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set OnClickListener for the "earnmore" button
        earnmore.setOnClickListener {
            // Retrieve the agent's phone number from the intent
            val contactNumber = intent.getStringExtra("globalPhoneNumber")

            // Create an intent to navigate to NewCardMarketAct
            val newCardMarketIntent = Intent(this, NewCardMarketAct::class.java)
            newCardMarketIntent.putExtra("CONTACT_NUMBER", contactNumber)

            // Start the activity with the new intent
            startActivity(newCardMarketIntent)

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Show the custom dialog
        dialog.show()
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

    private fun fetchUserDataFromAgentsCollection(parentName: String?) {
        parentName?.let {
            Toast.makeText(this, "parentName$parentName", Toast.LENGTH_SHORT).show()
            // Access Firestore collection and retrieve user documents
            val usersCollection = firestore.collection("users_agents")
            usersCollection.get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        // Check if the document has a "brand" field and it matches the parentName
                        if (document.contains("brand") && document.getString("brand") == parentName) {
                            // Fetch other required fields
                            val agentPhoneNumber = document.getString("phoneNumber")
                            val agentEmailAddress = document.getString("emailAddress")

                            // Append the data to resultText
                            val resultText = "$agentPhoneNumber\n$agentEmailAddress"
                            binding.textView121.text = resultText
                            Toast.makeText(this, "Data fetched successfully$resultText", Toast.LENGTH_SHORT).show()

                            binding.dialogPositiveButton1.visibility = View.VISIBLE

                            // If you only need to process the first matching document, you can break the loop here.
                            break
                        }
                    }

                    // If no matching document is found, display a message
                    if (binding.textView121.text.isBlank()) {
                        val message = "No matching document found in 'users_agents' collection for brand: $parentName"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        Log.d("FetchUserData", message)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    val errorMessage = "Failed to fetch data from users_agents: ${exception.message}"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("FetchUserData", errorMessage, exception)
                }
        } ?: run {
            // Handle the case where parentName is null
            val errorMessage = "Parent name is null"
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
        // Retrieve necessary data from the intent
        val intent = intent
        val globalPhoneNumber = intent.getStringExtra("globalPhoneNumber")
        val parentName = intent.getStringExtra("parentName")

        // Construct a "path" for the file (it will look like a folder structure)
        val folderPath = "$globalPhoneNumber/$parentName"



        // Use the path to create a reference to the file in Firebase Storage
        val storageRef = storage.reference.child(folderPath)

        // Proceed with file upload, Firebase Storage will create necessary paths
        storageRef.putBytes(byteArrayOf())
            .addOnSuccessListener {
                dismissLoadingDialog()
                Toast.makeText(this, "Folder created or already exists", Toast.LENGTH_SHORT).show()
                currentFolderName = folderPath

                saveFolderInfoToFirestore(globalPhoneNumber, parentName, currentFolderName)
                // Now that the "folder" is created, allow the user to pick/upload files
                pickAndUploadFiles()
            }
            .addOnFailureListener { e ->
                dismissLoadingDialog()
                Toast.makeText(this, "Failed to create folder: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFolderInfoToFirestore(globalPhoneNumber: String?, parentName: String?, folderPath: String?) {
        // Check if any of the necessary information is null
        if (globalPhoneNumber != null && parentName != null && folderPath != null) {
            // Access Firestore collection and document
            val usersCollection = firestore.collection("users")
            val userDocument = usersCollection.document(globalPhoneNumber)

            // Inner collection for the parentName
            val parentCollection = userDocument.collection(parentName)

            // Check if the document named "card_assets" exists
            parentCollection.document("card_assets")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // If the document exists, update the currentFolderName field
                        parentCollection.document("card_assets")
                            .update(mapOf("currentFolderName" to folderPath))
                            .addOnSuccessListener {
                                // Display the fetched folder path using a Toast
                                val pathFetched = documentSnapshot.getString("currentFolderName")
                                Toast.makeText(this, "Folder information updated in Firestore. Path Fetched: $pathFetched", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update folder information in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // If the document doesn't exist, create it with the currentFolderName field
                        parentCollection.document("card_assets")
                            .set(mapOf("currentFolderName" to folderPath))
                            .addOnSuccessListener {
                                // Display the fetched folder path using a Toast
                                val pathFetched = folderPath
                                Toast.makeText(this, "Folder information saved to Firestore. Path Fetched: $pathFetched", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save folder information to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to check if 'card_assets' document exists: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid data for Firestore update", Toast.LENGTH_SHORT).show()
        }
    }



    private fun pickAndUploadFiles() {
        // Use an Intent to open a file picker or any mechanism for uploading files
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Allow all file types
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { fileUri ->
                // Use the currentFolderName to upload the file to the correct folder
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
            // Show loader while uploading
            showLoadingDialog()

            val fileName = fileUri.lastPathSegment ?: ""

            val inputStream = contentResolver.openInputStream(fileUri)
            val fileHash = inputStream?.let { calculateHash(it) }

            // Check if the file is already uploaded
            if (uploadedFileHashes.contains(fileHash)) {
                // File already exists, show toast and dismiss loader
                dismissLoadingDialog()
                showToast("You already have this file in the folder")
            } else {
                val storageRef = storage.reference.child("$folder/$fileHash")

                storageRef.putFile(fileUri)
                    .addOnSuccessListener {
                        // Fetch the list of items in the folder after a successful upload
                        storageRef.parent?.listAll()
                            ?.addOnSuccessListener { listResult ->
                                // Get the total number of items in the folder
                                val totalItems = listResult.items.size

                                if (totalItems >= 10) {
                                    updateCoinsInFirestore(
                                        intent.getStringExtra("globalPhoneNumber"),
                                        folder,
                                        totalItems
                                    )
                                }


                                showToast("File uploaded successfully\nTotal items in folder: $totalItems")
                            }
                            ?.addOnFailureListener { e ->
                                showToast("Failed to get the total number of items in the folder: ${e.message}")
                            }

                        // Dismiss loader on success
                        dismissLoadingDialog()
                        // Add the uploaded file hash to the list
                        if (fileHash != null) {
                            uploadedFileHashes.add(fileHash)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Dismiss loader on failure
                        dismissLoadingDialog()
                        showToast("Failed to upload file: ${e.message}")
                    }
            }
        }
    }

    private fun updateCoinsInFirestore(contactNumber: String?, folder: String?, totalItems: Int) {
        contactNumber?.let { phoneNumber ->
            folder?.let { folderName ->
                // Access Firestore collection and document
                val usersCollection = firestore.collection("users")
                val userDocument = usersCollection.document(phoneNumber)

                // Inner collection "coins"
                val coinsCollection = userDocument.collection("coins")

                // Document "hushhcoins"
                val hushhCoinsDocument = coinsCollection.document("hushhcoins")

                // Use FieldPath to handle special characters in the field name
                val fieldName = FieldPath.of("${folder}dataupload")

                val maxValue = 100 // Set your maximum value here

                // Check if the aggregation field already exists
                hushhCoinsDocument.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Field exists, update its value with a limit
                            val newValue = totalItems * 20
                            val limitedValue = newValue.coerceAtMost(maxValue) // Limit the value to the maximum
                            hushhCoinsDocument
                                .update(fieldName, limitedValue)
                                .addOnSuccessListener {
                                    showToast("Coins updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    showToast("Failed to update coins: ${e.message}")
                                }
                        } else {
                            // Field doesn't exist, create it with a limit
                            val newValue = totalItems * 20
                            val limitedValue = newValue.coerceAtMost(maxValue) // Limit the value to the maximum
                            val data = hashMapOf(fieldName to limitedValue)
                            hushhCoinsDocument
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener {
                                    showToast("Coins field created and updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    showToast("Failed to create and update coins field: ${e.message}")
                                }
                        }
                    } else {
                        showToast("Failed to check if the aggregation field exists: ${task.exception?.message}")
                    }
                }
            } ?: run {
                showToast("Invalid folder name for Firestore update")
            }
        } ?: run {
            showToast("Invalid contact number for Firestore update")
        }
    }







    private fun shareCardAsImage() {
        // Capture the CardView as a bitmap
        val cardBitmap = getBitmapFromView(card1)

        // Save the bitmap to a temporary file
        val file = saveBitmapToFile(cardBitmap)

        // Create a content URI for the file using FileProvider
        val contentUri = FileProvider.getUriForFile(
            this,
            "com.example.project_gemini.fileprovider",
            file
        )

        // Share the image using an Intent
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
                    // Handle failure
                    Toast.makeText(this, "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT).show()
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
        // Load image into ImageView using Glide
        Glide.with(this)
            .load(imageURL)
            .placeholder(R.drawable.logo_splash_screen) // Placeholder image while loading
            // Error image if loading fails
            .into(binding.cardImage)
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

    private fun getDownloadLink(folderPath: String) {
        // Check if folderPath is not null or empty
        if (folderPath.isNotBlank()) {
            val storageRef = storage.reference.child(folderPath)

            // Get a list of items (files) in the folder
            storageRef.listAll()
                .addOnSuccessListener { listResult ->
                    val downloadUrls = mutableListOf<String>()

                    // Iterate through each item and get the download URL
                    listResult.items.forEach { item ->
                        item.downloadUrl
                            .addOnSuccessListener { uri ->
                                downloadUrls.add(uri.toString())

                                // Check if we have retrieved all download URLs
                                if (downloadUrls.size == listResult.items.size) {
                                    // Concatenate the URLs into a single string separated by commas
                                    val concatenatedUrls = downloadUrls.joinToString("  +hushh+  ")
                                    // Now you can use concatenatedUrls as needed (e.g., share or display)
                                    setQrInDataCard(concatenatedUrls)
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to get download link for ${item.name}: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to list items in the folder: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid folder path", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareDataCardAsImage(qrCodeContent: String) {
        val cardBitmap = getBitmapFromView(binding.cardShareInfo)

        // Save the bitmap to a temporary file
        val file = saveBitmapToFile(cardBitmap)

        // Create a content URI for the file using FileProvider
        val contentUri = FileProvider.getUriForFile(
            this,
            "com.example.project_gemini.fileprovider",
            file
        )

        // Share the image using an Intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Set the QR code content for the card before sharing


        // Start the activity for sharing
        startActivity(Intent.createChooser(shareIntent, "Share Card"))
    }

    private fun setQrInDataCard(qrCodeContent: String) {
        // Generate QR code bitmap
        val qrCodeBitmap = generateQrCodeBitmap(qrCodeContent)

        // Set the QR code bitmap to ImageView with id "imageView174"
        binding.imageView174.setImageBitmap(qrCodeBitmap)
        shareDataCardAsImage(qrCodeContent)

    }

    private fun generateQrCodeBitmap(content: String): Bitmap? {
        try {
            // Set up the QR code parameters
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2 // Set margin for the QR code

            // Encode the content as QR code
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints)

            // Create a Bitmap from the bit matrix
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

            return bitmap

        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }


    private fun shareFolderLinks(links: String) {
        // Share the links using an Intent
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
