package com.example.mptp2023

import android.app.Dialog
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mptp2023.databinding.DialogExpenseDetailsBinding
import com.example.mptp2023.databinding.ItemExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class ExpensesAdapter : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {
    private val expensesList: MutableList<Expense> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expensesList[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int {
        return expensesList.size
    }

    fun updateExpenses(expenses: List<Expense>) {
        expensesList.clear()
        expensesList.addAll(expenses)
        notifyDataSetChanged()
    }
    // Modify the ExpensesAdapter to display expense names
    fun formatJsonObject(jsonObject: JSONObject, indent: Int = 0): String {
        val stringBuilder = StringBuilder()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject[key]
            val formattedKey = "${indentSpaces(indent)}$key"
            val formattedValue = when (value) {
                is JSONObject -> formatJsonObject(value, indent + 1)
                is JSONArray -> formatJsonArray(value, indent + 1)
                else -> value.toString()
            }
            stringBuilder.append("$formattedKey: $formattedValue\n")
        }
        return stringBuilder.toString()
    }

    // Function to format the JSON array with proper indentation
    fun formatJsonArray(jsonArray: JSONArray, indent: Int = 0): String {
        val stringBuilder = StringBuilder()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray[i]
            val formattedValue = when (value) {
                is JSONObject -> formatJsonObject(value, indent + 1)
                is JSONArray -> formatJsonArray(value, indent + 1)
                else -> value.toString()
            }
            stringBuilder.append("${indentSpaces(indent)}$formattedValue\n")
        }
        return stringBuilder.toString()
    }

    // Function to generate spaces for indentation
    fun indentSpaces(indent: Int): String {
        return " ".repeat(indent * 4)
    }
    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expenseTextView.text = expense.name

            binding.root.setOnClickListener {
                val dialog = Dialog(binding.root.context) // Use the context of the activity or view where the ExpensesAdapter is used
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                val dialogBinding = DialogExpenseDetailsBinding.inflate(LayoutInflater.from(dialog.context))
                dialog.setContentView(dialogBinding.root)
                dialog.setCancelable(true)

                val jsonString:String = expense.amount
                val jsonObject = JSONObject(jsonString.replace("'", "\""))
                val formattedJson = formatJsonObject(jsonObject)

                dialogBinding.expenseAmountTextView.text = formattedJson

                // Fetch the image from Firebase Storage and display it in the dialog
                val storage = Firebase.storage("gs://mptp2023.appspot.com")
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usersRef = storage.reference.child("Users")
                    val usernameRef = usersRef.child("${currentUser?.uid}")
                    val fileName = expense.key + ".jpg"
                    Log.d("iise","${expense.amount}")
                    Log.d("iise","${expense.name}")
                    Log.d("iise","${expense.key}")

                    val photoRef = usernameRef.child(fileName)


                    val localFile = File.createTempFile("tempImage", "jpg")
                    photoRef.getFile(localFile)
                        .addOnSuccessListener {
                            // Successfully downloaded the image, now display it in the dialog
                            val myBitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            dialogBinding.expenseImageView.setImageBitmap(myBitmap)
                            dialog.show()
                        }
                        .addOnFailureListener { exception ->
                            // Handle errors here
                        }
                } else {
                    // Handle user not logged in here

                }
            }
        }
    }

}