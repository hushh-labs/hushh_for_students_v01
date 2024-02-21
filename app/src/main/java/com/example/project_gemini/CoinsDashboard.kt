package com.example.project_gemini

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_gemini.databinding.ActivityCoinsDashboardBinding
import com.example.project_gemini.databinding.LayoutDashboardItemBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class CoinsDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityCoinsDashboardBinding
    private val usersAdapter = UsersAdapter()


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoinsDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView with LinearLayoutManager
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = usersAdapter

        // Set click listener for capturing screenshot and sharing
            binding.linearLayout3.setOnClickListener {
            val screenshotBitmap = captureScreenshot(binding.CoinsDashboardlayout)
            if (screenshotBitmap != null) {
                // Share the screenshot
                shareScreenshot(screenshotBitmap)
            } else {
                showToast("Failed to capture screenshot.")
            }
        }


        // Retrieve all documents from the "users" collection and sort based on "CurrentTotalCoins"
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection
            .orderBy("CurrentTotalCoins", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Iterate through the top 3 documents
                querySnapshot.documents.forEachIndexed { index, documentSnapshot ->
                    val firstName = documentSnapshot.getString("firstName")
                    val totalCoins = documentSnapshot.get("CurrentTotalCoins")

                    // Check if totalCoins is not null and is of type Long
                    if (totalCoins is Long) {
                        when (index) {
                            0 -> {
                                binding.textView23.text = firstName
                                binding.textView26.text = totalCoins.toString()
                            }
                            1 -> {
                                binding.textView22.text = firstName
                                binding.textView25.text = totalCoins.toString()
                            }
                            2 -> {
                                binding.textView24.text = firstName
                                binding.textView27.text = totalCoins.toString()
                            }
                        }
                    } else {
                        // Handle the case when "CurrentTotalCoins" is not a Long
                        showToast("Invalid or null CurrentTotalCoins for user: $firstName")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve data: ${e.message}", Toast.LENGTH_SHORT).show()
            }



    // Retrieve all documents from the "users" collection and sort based on "CurrentTotalCoins"
    val usersRecyclerviewCollection = FirebaseFirestore.getInstance().collection("users")
        usersRecyclerviewCollection.orderBy("CurrentTotalCoins", com.google.firebase.firestore.Query.Direction.DESCENDING)
    .limit(10)
    .get()
    .addOnSuccessListener { querySnapshot ->
        val usersList = mutableListOf<UserItem>()

        // Iterate through the documents
        querySnapshot.documents.forEachIndexed { index, documentSnapshot ->
            val firstName = documentSnapshot.getString("firstName")
            val totalCoins = documentSnapshot.getLong("CurrentTotalCoins")

            // Check if totalCoins is not null
            if (totalCoins != null) {
                // Create a UserItem and add it to the list
                val userItem = UserItem(index + 1, firstName.orEmpty(), totalCoins)
                usersList.add(userItem)
            } else {
                // Handle the case when "CurrentTotalCoins" is null
                showToast("Null CurrentTotalCoins for user: $firstName")
            }
        }

        // Update the adapter with the new list of users
        usersAdapter.userList = usersList

    }
    .addOnFailureListener { e ->
        Toast.makeText(this, "Failed to retrieve data: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

    private fun captureScreenshot(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun shareScreenshot(bitmap: Bitmap) {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "CoinsDashboard_Screenshot",
            null
        )

        val uri = Uri.parse(path)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpeg"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

data class UserItem(
    val serialNumber: Int,
    val name: String,
    val totalCoins: Long
)

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    var userList: List<UserItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            LayoutDashboardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    class UserViewHolder(private val binding: LayoutDashboardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userItem: UserItem) {
            binding.textserno.text = userItem.serialNumber.toString()
            binding.textname.text = userItem.name
            binding.texttotalcoins.text = userItem.totalCoins.toString()
            // You can set the profile picture and other views here if needed
        }
    }
}
