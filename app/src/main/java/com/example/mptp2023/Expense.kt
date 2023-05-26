package com.example.mptp2023

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Expense(
    val amount: Double = 0.0,
    val name: String = "default",
    val key: String = ""
) {
    constructor(snapshot: DataSnapshot) : this(
        snapshot.child("amount").value as Double,
        snapshot.child("name").value as String,
        snapshot.key ?: ""
    )
}