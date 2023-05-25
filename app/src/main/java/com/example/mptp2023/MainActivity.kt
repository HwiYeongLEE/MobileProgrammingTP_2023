package com.example.mptp2023

import android.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var expensesAdapter: ExpensesAdapter

    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val photoUri = result.data?.data
            // Process the photo using OCR API and handle the JSON response
            // For now, let's assume the API response is {'amount': 1.0}
            val ocrApiResponse = "{'amount': 1.0}"
            val resObject = JSONObject(ocrApiResponse)
            // Save the data to the database and update the UI
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val expensesRef = database.child("expenses").child(currentUser.uid).child(currentDate)
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

    private fun retrieveExpensesForDate(date: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val expensesRef = database.child("expenses").child(currentUser.uid).child(date)
            expensesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val expensesList = mutableListOf<Expense>()
                    for (expenseSnapshot in dataSnapshot.children) {
                        val expenseName = expenseSnapshot.child("name").getValue(String::class.java)
                        if (expenseName != null) {
                            expensesList.add(Expense(name = expenseName))
                        }
                    }
                    expensesAdapter.updateExpenses(expensesList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
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

        val spinner = binding.dateSpinner

//        val expensesListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val expensesList = mutableListOf<Expense>()
//                for (expenseSnapshot in dataSnapshot.children) {
//                    val expense = expenseSnapshot.getValue(Expense::class.java)
//                    expense?.let { expensesList.add(it) }
//                }
//                expensesAdapter.updateExpenses(expensesList)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle database error
//            }
//        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val expensesRef = database.child("expenses").child(currentUser.uid)
            expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dates = mutableListOf<String>()
                    for (dateSnapshot in dataSnapshot.children) {
                        dates.add(dateSnapshot.key.toString())
                    }
                    val adapter = ArrayAdapter(this@MainActivity, R.layout.simple_spinner_item, dates)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        }

        // Set a listener for date selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDate = parent.getItemAtPosition(position).toString()
                // Retrieve expenses for the selected date and update the adapter
                retrieveExpensesForDate(selectedDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
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
