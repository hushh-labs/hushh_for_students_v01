package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class QuestionBottomSheetFragment(private val title: String, private val contactNumber: String?) : BottomSheetDialogFragment() {
    private var currentQuestionIndex = 0
    private lateinit var radioGroup: RadioGroup
    private lateinit var questionTextView: TextView
    private var currentQuestionCategory: QuestionCategory = QuestionCategories.FASHION // Initialize with a default category

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_bottom_sheet, container, false)
        radioGroup = view.findViewById(R.id.radioGroup)
        questionTextView = view.findViewById(R.id.question)
        val nextButton = view.findViewById<ImageButton>(R.id.nextbutton)
        nextButton.setOnClickListener {
            onNextButtonClick()
        }

        when (title) {
            "Allergy Card" -> currentQuestionCategory = QuestionCategories.ALLERGY_CARD
            "Fashion and Style preference" -> currentQuestionCategory = QuestionCategories.FASHION_STYLE_PREFERENCE
            "Payment data card" -> currentQuestionCategory = QuestionCategories.PAYMENT_DATA_CARD
            "Purchase History card" -> currentQuestionCategory = QuestionCategories.PURCHASE_HISTORY_CARD
            "Social Media Activity" -> currentQuestionCategory = QuestionCategories.SOCIAL_MEDIA_CARD
        }

        // Call setupQuestion after setting the category
        setupQuestion()

        // Fetch and display the category only if it's not a predefined title
        if (currentQuestionCategory == QuestionCategories.FASHION) {
            fetchCategoryFromFirestore()
        }

        return view
    }

    private fun fetchCategoryFromFirestore() {
        contactNumber?.let {
            firestore.collection("buisness_onboard")
                .document(title)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val category = documentSnapshot.getString("Category")
                        category?.let { fetchedCategory ->
                            // Commented out non-essential Toast
                            // Toast.makeText(context, "$title", Toast.LENGTH_SHORT).show()

                            val matchingIndex = findMatchingIndex(fetchedCategory)
                            if (matchingIndex != -1) {
                                // Commented out non-essential Toast
                                // Toast.makeText(context, "Matched category at index $matchingIndex: $fetchedCategory", Toast.LENGTH_SHORT).show()

                                // Set the current question category based on the matching index
                                currentQuestionCategory = when (matchingIndex) {
                                    1 -> QuestionCategories.FASHION
                                    2 -> QuestionCategories.INSURANCE
                                    3 -> QuestionCategories.CULINARY
                                    4 -> QuestionCategories.TRAVEL
                                    5 -> QuestionCategories.GENERAL
                                    6 -> QuestionCategories.HOTEL
                                    else -> throw IllegalArgumentException("Invalid matching index")
                                }

                                // Call setupQuestion after setting the category
                                setupQuestion()
                            } else {
                                // Commented out non-essential Toast
                                // Toast.makeText(context, "Category '$fetchedCategory' does not match any predefined category.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Document does not exist
                        // Commented out non-essential Toast
                        // Toast.makeText(context, "Document not found for title '$title'", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(context, "Failed to fetch category: ${e.message}", Toast.LENGTH_SHORT).show() // Important: Inform the user of the failure
                }
        }
    }

    private fun findMatchingIndex(category: String): Int {
        val predefinedCategories = arrayOf(
            "Fashion, Dress, Personal",
            "Health, Life, Property : Insurance",
            "Culinary, Unwind, Leisure",
            "Travel, Roam, Explore",
            "Shopping, Hunt, Obtain",
            "Hospitality, Stay, Accommodation"
        )

        for ((index, predefinedCategory) in predefinedCategories.withIndex()) {
            if (category == predefinedCategory) {
                return index + 1 // Adding 1 to convert from 0-based index to 1-based index
            }
        }

        return -1 // Not found
    }

    private fun setupQuestion() {
        radioGroup.removeAllViews()
        val questionText = currentQuestionCategory.questions[currentQuestionIndex]
        val options = currentQuestionCategory.optionsProvider(currentQuestionIndex)

        // Set the question text
        questionTextView.text = questionText

        for ((index, option) in options.withIndex()) {
            val radioButton = RadioButton(context)
            radioButton.text = option
            radioButton.id = index
            radioGroup.addView(radioButton)
        }
    }

    private fun onNextButtonClick() {
        val checkedRadioButtonId = radioGroup.checkedRadioButtonId
        if (checkedRadioButtonId != -1) {
            // User selected an option
            val selectedOption = currentQuestionCategory.optionsProvider(currentQuestionIndex)[checkedRadioButtonId]
            saveResponseToFirestore(currentQuestionIndex, selectedOption)

            if (currentQuestionIndex < currentQuestionCategory.questions.size - 1) {
                // Move to the next question
                currentQuestionIndex++
                setupQuestion()
            } else {
                // All questions answered, close the dialog and show success message
                dismiss()
                Toast.makeText(context, "Awesome! You are done with the last question.", Toast.LENGTH_SHORT).show() // Important: Inform the user of the success

                // Transfer to ShowFinalCardAct
                val intent = Intent(context, ShowFinalCardAct::class.java)
                intent.putExtra("title", title)
                intent.putExtra("contactNumber", contactNumber)
                startActivity(intent)
            }
        } else {
            // User needs to select an option
            Toast.makeText(context, "Please select an option", Toast.LENGTH_SHORT).show() // Important: Inform the user of the required action
        }
    }

    private fun saveResponseToFirestore(questionIndex: Int, selectedOption: String) {
        // Create a map to represent the user's response
        val responseMap = hashMapOf(
            "question" to currentQuestionCategory.questions[questionIndex],
            "answer" to selectedOption
        )

        responseMap["title"] = title

        // Store the response in Firestore
        contactNumber?.let {
            val userDocument = firestore.collection("users").document(it)
            val responseCollection = userDocument.collection(title)

            // Check if a document with the same question already exists
            responseCollection
                .whereEqualTo("question", currentQuestionCategory.questions[questionIndex])
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // If a document with the same question exists, update it
                        responseCollection.document(document.id)
                            .set(responseMap, SetOptions.merge()) // Use SetOptions.merge() to update without overwriting
                            .addOnSuccessListener {
                                // Successfully stored the updated response in Firestore
                                updateTitleCompletion(it.toString(), document.id)
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Toast.makeText(context, "Failed to update response: ${e.message}", Toast.LENGTH_SHORT).show() // Important: Inform the user of the failure
                            }
                        return@addOnSuccessListener
                    }

                    // If no document with the same question exists, add a new one
                    responseCollection
                        .add(responseMap)
                        .addOnSuccessListener { documentReference ->
                            // Successfully stored the response in Firestore
                            updateTitleCompletion(it, documentReference.id)
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            Toast.makeText(context, "Failed to store response: ${e.message}", Toast.LENGTH_SHORT).show() // Important: Inform the user of the failure
                        }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(context, "Failed to check for existing response: ${e.message}", Toast.LENGTH_SHORT).show() // Important: Inform the user of the failure
                }
        }
    }

    private fun updateTitleCompletion(contactNumber: String, documentId: String) {
        context?.let { nonNullContext ->
            // Update the "title" field in the user's document to indicate completion
            firestore.collection("users")
                .document(contactNumber)
                .update(mapOf(title to true))
                .addOnSuccessListener {
                    // Successfully updated the "title" field
                    Toast.makeText(nonNullContext, "Title completion updated successfully!", Toast.LENGTH_SHORT).show() // Important: Inform the user of the success

                    // Add field in the "coins" collection as described
                    firestore.collection("users")
                        .document(contactNumber)
                        .collection("coins")
                        .document("hushhcoins")
                        .update(mapOf(title to 50)) // Assuming 50 is the value you want to set
                        .addOnSuccessListener {
                            // Successfully updated the "hushhcoins" field
                            Toast.makeText(nonNullContext, "Coins updated successfully!", Toast.LENGTH_SHORT).show()
                        // Important: Inform the user of the success
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            Toast.makeText(nonNullContext, "Failed to update coins: ${e.message}", Toast.LENGTH_SHORT).show()
                        // Important: Inform the user of the failure
                        }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(nonNullContext, "Failed to update title completion: ${e.message}", Toast.LENGTH_SHORT).show()
                // Important: Inform the user of the failure
                }
        }
    }
}
