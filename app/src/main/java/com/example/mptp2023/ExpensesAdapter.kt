package com.example.mptp2023

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expenseTextView.text = expense.amount.toString()
        }
    }
}
