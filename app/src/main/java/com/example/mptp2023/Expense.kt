package com.example.mptp2023

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Expense(
    val amount: String = "default",
    val name: String = "default",
    val key: String = ""
) {
    constructor(snapshot: DataSnapshot) : this(
        snapshot.child("amount").value as String,
        snapshot.child("name").value as String,
        snapshot.key ?: ""
    )
}