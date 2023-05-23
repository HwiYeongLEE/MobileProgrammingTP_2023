package com.example.mptp2023

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.example.mptp2023.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var expensesAdapter: ExpensesAdapter

    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoUri = result.data?.data
            // Process the photo using OCR API and handle the JSON response
            // For now, let's assume the API response is {'amount': 1.0}
            val ocrApiResponse = "{'amount': 1.0}"
            val resObject = JSONObject(ocrApiResponse)
            // Save the data to the database and update the UI
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val expensesRef = database.child("expenses").child(currentUser.uid)
                val newExpenseKey = expensesRef.push().key
                val newExpense = Expense(amount=resObject.getDouble("amount")) // Create a new Expense object with the OCR API response
                if (newExpenseKey != null) {
                    expensesRef.child(newExpenseKey).setValue(newExpense)
                        .addOnSuccessListener {
                            showToast("Data saved successfully!")
                        }
                        .addOnFailureListener { exception ->
                            showToast("Failed to save data: ${exception.message}")
                        }
                }
            }
        }
    }

    private lateinit var binding: ActivityMainBinding

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database("https://mptp2023-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        expensesAdapter = ExpensesAdapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = expensesAdapter

        val expensesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val expensesList = mutableListOf<Expense>()
                for (expenseSnapshot in dataSnapshot.children) {
                    val expense = expenseSnapshot.getValue(Expense::class.java)
                    expense?.let { expensesList.add(it) }
                }
                expensesAdapter.updateExpenses(expensesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val expensesRef = database.child("expenses").child(currentUser.uid)
            expensesRef.addValueEventListener(expensesListener)
        }

        binding.addButton.setOnClickListener {
            takePicture.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            showToast("Logged out successfully!")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
            if (currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            else {
                showToast("You're logged in already!")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
