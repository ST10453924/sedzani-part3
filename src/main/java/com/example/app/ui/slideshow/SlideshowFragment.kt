package com.example.app.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.data.BudgetDbHelper
import com.example.app.databinding.FragmentSlideshowBinding
import java.text.SimpleDateFormat
import java.util.*

class SlideshowFragment : Fragment() {

    // ViewBinding reference for accessing UI elements
    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    // Reference to the database helper class for budget data
    private lateinit var dbHelper: BudgetDbHelper

    // Called to inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        dbHelper = BudgetDbHelper(requireContext())

        // Set up spinner with budget categories
        setupCategorySpinner()

        // Configure button click listeners
        setupBudgetButtons()

        return binding.root
    }

    // Populates the spinner with predefined budget categories
    private fun setupCategorySpinner() {
        val categories = listOf(
            "Housing", "Utilities", "Transportation", "Food",
            "Healthcare", "Insurance", "Debt Payments",
            "Personal & Family", "Entertainment",
            "Savings & Investments", "Miscellaneous"
        )

        // Create an ArrayAdapter to link categories with the spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    // Configures click listeners for both budget-setting buttons
    private fun setupBudgetButtons() {
        binding.btnSetCategoryBudget.setOnClickListener {
            handleCategoryBudgetInput()
        }

        binding.btnSetMonthlyBudget.setOnClickListener {
            handleMonthlyBudgetInput()
        }
    }

    // Handles input and saving logic for category-specific budgets
    private fun handleCategoryBudgetInput() {
        val category = binding.categorySpinner.selectedItem.toString()
        val amountText = binding.budgetLimitInput.text.toString().trim()

        if (amountText.isEmpty()) {
            showError("Please enter budget amount")
            return
        }

        try {
            val amount = amountText.toDouble()
            // Save category budget to DB
            val id = dbHelper.addCategoryBudget(category, amount)

            if (id != -1L) {
                showSuccess("$category budget set to ${formatCurrency(amount)}")
                binding.budgetLimitInput.text?.clear()
            } else {
                showError("Failed to save category budget")
            }
        } catch (e: NumberFormatException) {
            showError("Invalid amount format")
        }
    }

    // Handles input and saving logic for full monthly budgets
    private fun handleMonthlyBudgetInput() {
        val monthText = binding.monthlyMonthInput.text.toString().trim()
        val amountText = binding.monthlyBudgetInput.text.toString().trim()

        if (monthText.isEmpty() || amountText.isEmpty()) {
            showError("Please enter both month and amount")
            return
        }

        try {
            // Ensure the month string is in MM/yyyy format
            val dateFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(monthText) // Will throw if invalid

            val amount = amountText.toDouble()
            // Save the monthly budget to DB
            val id = dbHelper.addMonthlyBudget(monthText, amount)

            if (id != -1L) {
                showSuccess("Monthly budget for ${formatMonth(monthText)} set to ${formatCurrency(amount)}")
                clearMonthlyInputs()
            } else {
                showError("This month already has a budget set")
            }
        } catch (e: Exception) {
            showError("Invalid month format (use MM/YYYY) or amount")
        }
    }

    // Formats a double value into a string with two decimal places
    private fun formatCurrency(amount: Double): String {
        return "%.2f".format(amount)
    }

    // Converts a date string from MM/yyyy to a more readable format like "March 2025"
    private fun formatMonth(monthStr: String): String {
        val dateFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return displayFormat.format(dateFormat.parse(monthStr))
    }

    // Clears the input fields after successfully saving a monthly budget
    private fun clearMonthlyInputs() {
        binding.monthlyMonthInput.text?.clear()
        binding.monthlyBudgetInput.text?.clear()
    }

    // Displays an error message as a toast
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Displays a success message as a toast
    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Clean up references and close DB connection to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
