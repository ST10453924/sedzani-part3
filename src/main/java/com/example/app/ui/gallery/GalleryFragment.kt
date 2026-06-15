package com.example.app.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.data.ExpenseDbHelper
import com.example.app.databinding.FragmentGalleryBinding
import java.text.SimpleDateFormat
import java.util.*

class GalleryFragment : Fragment() {

    // ViewBinding instance for accessing layout views safely
    private var _binding: FragmentGalleryBinding? = null

    // Non-nullable reference to the binding
    private val binding get() = _binding!!

    // Reference to the SQLite database helper class
    private lateinit var dbHelper: ExpenseDbHelper

    // Called to create and return the fragment’s UI view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        // Initialize database helper
        dbHelper = ExpenseDbHelper(requireContext())

        // Populate the spinner with category options
        setupSpinner()

        // Set up button click listeners
        setupButtons()

        // Return the root view of the binding
        return binding.root
    }

    // Populates the category spinner with predefined options
    private fun setupSpinner() {
        val categories = listOf(
            "Housing", "Utilities", "Transportation", "Food",
            "Healthcare", "Insurance", "Debt Payments",
            "Personal & Family", "Entertainment",
            "Savings & Investments", "Miscellaneous"
        )

        // Create an ArrayAdapter using the categories list
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )

        // Set dropdown layout style for spinner items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach the adapter to the spinner
        binding.spinnerOptions.adapter = adapter
    }

    // Sets up functionality for the "Enter" and "Clear" buttons
    private fun setupButtons() {
        // Handle "Enter" button click
        binding.buttonEnter.setOnClickListener {
            val category = binding.spinnerOptions.selectedItem.toString()
            val title = binding.editTextTitle.text.toString().trim()
            val amountText = binding.editTextAmount.text.toString().trim()
            val date = binding.editTextDate.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim()

            // Validate all inputs before inserting into database
            if (validateInputs(title, amountText, date, description)) {
                try {
                    // Convert amount from text to double
                    val amount = amountText.toDouble()

                    // Use current date if date input is empty
                    val currentDate = if (date.isEmpty()) getCurrentDate() else date

                    // Currently empty; can later be updated to support image saving
                    val imagePath = ""

                    // Insert data into SQLite database
                    val id = dbHelper.addExpense(
                        category,
                        title,
                        amount,
                        currentDate,
                        description,
                        imagePath
                    )

                    // Show success or failure message
                    if (id != -1L) {
                        Toast.makeText(
                            requireContext(),
                            "Expense added successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        clearForm()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to add expense",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    // Handle invalid number input
                    Toast.makeText(
                        requireContext(),
                        "Invalid amount format",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Handle "Clear" button click to reset the form
        binding.buttonClear.setOnClickListener {
            clearForm()
        }
    }

    // Validates all form fields and shows error messages if invalid
    private fun validateInputs(
        title: String,
        amount: String,
        date: String,
        description: String
    ): Boolean {
        return when {
            title.isEmpty() -> {
                showError("Please enter a title")
                false
            }
            amount.isEmpty() -> {
                showError("Please enter an amount")
                false
            }
            date.isEmpty() -> {
                showError("Please enter a date")
                false
            }
            description.isEmpty() -> {
                showError("Please enter a description")
                false
            }
            else -> true // All inputs are valid
        }
    }

    // Utility function to show toast error messages
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Returns the current date in "yyyy-MM-dd" format
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Clears all input fields and resets the spinner and image preview
    private fun clearForm() {
        binding.editTextTitle.text?.clear()
        binding.editTextAmount.text?.clear()
        binding.editTextDate.text?.clear()
        binding.editTextDescription.text?.clear()
        binding.imagePreview.setImageDrawable(null)
        binding.spinnerOptions.setSelection(0)
    }

    // Called when the fragment view is destroyed; cleans up binding and database helper
    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
