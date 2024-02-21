package com.example.project_gemini.model

import com.google.firebase.firestore.PropertyName

data class UserModel(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val emailAddress: String = "",
    val birthday: String = "",


)

