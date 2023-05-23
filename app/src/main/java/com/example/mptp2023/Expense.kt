package com.example.mptp2023

data class Expense(
    val amount: Double = 0.0,
    // Add other fields related to the expense (e.g., date, category, etc.)
) {
    // Add a no-argument constructor
    constructor() : this(0.0)
}









