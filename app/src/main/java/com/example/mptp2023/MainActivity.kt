package com.example.mptp2023

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var storage: FirebaseStorage


    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            //val photoUri = result.data?.data
            //지금은 테스트를 위해 로컬의 이미지파일을 사용합니다.
            val packageName = BuildConfig.APPLICATION_ID
            val photoUri = Uri.parse("android.resource://$packageName/${com.example.mptp2023.R.drawable.my_image}")
            // Process the photo using OCR API and handle the JSON response
            // For now, let's assume the API response is {'amount': 1.0}
            val ocrApiResponse = "{'amount': 1.0}"
            val resObject = JSONObject(ocrApiResponse)



            val currentUser = auth.currentUser
            val expensesRef = database.child("expenses").child(currentUser!!.uid).child(currentDate)
            val newExpenseKey = expensesRef.push().key
            val newExpense = Expense(amount = resObject.getDouble("amount"), name = "john", key = newExpenseKey.toString())
            Log.d("iise!", "${newExpense}")
            Log.d("iise!", "${newExpense.key}")
            if (currentUser != null) {
                if (newExpenseKey != null) {
                    expensesRef.child(newExpenseKey).setValue(newExpense)
                        .addOnSuccessListener {
                            showToast("데이터가 성공적으로 저장되었습니다!")
                        }
                        .addOnFailureListener { exception ->
                            showToast("데이터 저장에 실패했습니다: ${exception.message}")
                        }
                }
            }


            //이미지파일을 스토리지에 추가합니다.
            if (photoUri != null) {
                // 사진의 이름을 랜덤하게 생성합니다. ex) "c367d973-5c4d-4e2a-9f17-721c048d3ecb.jpg"
                val fileName = newExpenseKey.toString() + ".jpg"

                // 저장하고 싶은 파일 위치로 이동합니다. 여기서는 Users/username/fileName.jpg 로 저장하려고 합니다.
                val storageRef = storage.reference
                val usersRef = storageRef.child("Users")
                val usernameRef = usersRef.child("${currentUser?.uid}")

                // 파일을 업로드합니다.
                val photoRef = usernameRef.child(fileName)
                val uploadTask = photoRef.putFile(photoUri)

                // 작업 수행에 대한 Toast를 출력합니다.
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // 성공 시
                    showToast("사진이 성공적으로 업로드되었습니다!")
                }.addOnFailureListener { exception ->
                    // 실패 시
                    showToast("사진 업로드에 실패했습니다: ${exception.message}")
                }
            } else {
                showToast("사진 URI를 가져오지 못했습니다")
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
                        val expenseAmount = expenseSnapshot.child("amount").getValue(Double::class.java)
                        val expenseKey = expenseSnapshot.key
                        if (expenseName != null && expenseAmount != null && expenseKey != null) {
                            expensesList.add(Expense(name = expenseName, amount = expenseAmount, key = expenseKey))
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
        //firebase의 스토리지를 주소를 통해 참조합니다.
        storage = Firebase.storage("gs://mptp2023.appspot.com")
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
                    val expensesList = mutableListOf<Expense>()
                    for (expenseSnapshot in dataSnapshot.children) {
                        val expense = expenseSnapshot.getValue(Expense::class.java)
                        if (expense != null) {
                            expensesList.add(Expense(amount = expense.amount, name = expense.name, key = expenseSnapshot.key.toString()))
                        }
                    }
                    expensesAdapter.updateExpenses(expensesList)
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