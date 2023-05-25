package com.example.mptp2023

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mptp2023.databinding.DialogExpenseDetailsBinding
import com.example.mptp2023.databinding.ItemExpenseBinding

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
    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expenseTextView.text = expense.name

            binding.root.setOnClickListener {
                val dialog = Dialog(binding.root.context) // Use the context of the activity or view where the ExpensesAdapter is used
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                val dialogBinding = DialogExpenseDetailsBinding.inflate(LayoutInflater.from(dialog.context))
                dialog.setContentView(dialogBinding.root)
                dialog.setCancelable(true)

                dialogBinding.expenseAmountTextView.text = "Amount: ${expense.amount}"

                dialog.show()
            }
        }
    }
}
