package com.example.project_gemini

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project_gemini.databinding.LayoutMenuItemBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HushhMenuAdapter(private val currentUserDocument: DocumentReference, private val CardName: String) :
    ListAdapter<HushhMenuItem, HushhMenuAdapter.ViewHolder>(HushhMenuItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class ViewHolder(private val binding: LayoutMenuItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: HushhMenuItem) {
            binding.textView2.text = menuItem.itemName
            binding.textView3.text = "Original Price - Rs ${menuItem.itemPrice}"
            binding.textView5.text = "Current Stock - ${menuItem.itemStock} items"

            binding.button.setOnClickListener {
                val currentStock = menuItem.itemStock.toInt()
                if (currentStock == 0) {
                    Toast.makeText(binding.root.context, "This item is out of stock.", Toast.LENGTH_SHORT).show()
                } else {
                    showAddDialog(menuItem)
                }
            }

            binding.button11.setOnClickListener {
                deleteItem(menuItem.itemName)
            }




            setButtonText(menuItem.itemName) // Set the initial button text
        }

        private fun deleteItem(itemName: String) {
            val firestore = FirebaseFirestore.getInstance()
            val orderCollection = currentUserDocument.collection("ordersfrommenu")
            orderCollection.document(itemName)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "Item deleted successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "Failed to delete item.", Toast.LENGTH_SHORT).show()
                }
        }


        private fun showAddDialog(menuItem: HushhMenuItem) {
            val builder = AlertDialog.Builder(binding.root.context)
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogLayout = inflater.inflate(R.layout.dialog_add_item, null)
            builder.setView(dialogLayout)

            builder.setPositiveButton("Add") { _, _ ->
                // Fetch user input
                val userInputOfStock = dialogLayout.findViewById<EditText>(R.id.editTextQuantity).text.toString().toIntOrNull()

                userInputOfStock?.let { userInput ->
                    if (userInput <= menuItem.itemStock.toInt()) {
                        // Add order to Firestore
                        val firestore = FirebaseFirestore.getInstance()
                        val orderCollection = currentUserDocument.collection("ordersfrommenu")
                        val totalItemCost = (userInput * menuItem.itemPrice.toInt()).toString()

                        val orderData = hashMapOf(
                            "itemPrice" to menuItem.itemPrice,
                            "userInputOfStock" to userInput.toString(),
                            "timestamp" to SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(Date()),
                            "paymentConfirmed" to "no",
                            "orderConfirmed" to "no",
                            "orderFromBusiness" to CardName, // Set the orderFromBusiness field
                            "totalItemCost" to totalItemCost // Set the totalItemCost field
                        )


                        // Create a document with the item name as the document ID
                        orderCollection.document(menuItem.itemName).set(orderData)
                            .addOnSuccessListener {
                                Toast.makeText(binding.root.context, "Item added successfully.", Toast.LENGTH_SHORT).show()
                                setButtonText(menuItem.itemName) // Update the button text after adding the item
                            }
                            .addOnFailureListener {
                                Toast.makeText(binding.root.context, "Failed to add item.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(binding.root.context, "Invalid quantity entered.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

        private fun setButtonText(itemName: String) {
            // Fetch "userInputOfStock" from Firebase Firestore and set it in buttonText
            val orderCollection = currentUserDocument.collection("ordersfrommenu")
            val documentRef = orderCollection.document(itemName)
            documentRef.addSnapshotListener { documentSnapshot, _ ->
                val userInputOfStock = documentSnapshot?.getString("userInputOfStock")
                binding.buttonText.text = userInputOfStock ?: "Add" // Set the button text to userInputOfStock or "Add" if it's null
            }
        }
    }

}

class HushhMenuItemDiffCallback : DiffUtil.ItemCallback<HushhMenuItem>() {
    override fun areItemsTheSame(oldItem: HushhMenuItem, newItem: HushhMenuItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: HushhMenuItem, newItem: HushhMenuItem): Boolean {
        return oldItem.itemName == newItem.itemName &&
                oldItem.itemPrice == newItem.itemPrice &&
                oldItem.itemStock == newItem.itemStock
    }
}
