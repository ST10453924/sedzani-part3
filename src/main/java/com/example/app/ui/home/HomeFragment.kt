package com.example.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.data.DatabaseHelper
import com.example.app.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    // ViewBinding reference to interact with UI elements safely
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("ViewBinding is null")

    // SQLite database helper instance
    private lateinit var dbHelper: DatabaseHelper

    // Formatter to display currency (e.g., R123.45)
    private val currencyFormat = NumberFormat.getCurrencyInstance()

    // Formatter for month spinner format (e.g., 05/2025)
    private val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    // Formatter for expense date display (e.g., 12 May 2025)
    private val expenseDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        // Constants for cell padding and height in dp units
        private const val CELL_PADDING_DP = 8
        private const val CELL_MIN_HEIGHT_DP = 48
    }

    // Inflates the layout and initializes database and UI setup
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupMonthSpinner()  // Set up the spinner with available months
        initializeUI()       // Set listeners and load initial data

        return binding.root
    }

    // Initializes listeners and default UI behavior
    private fun initializeUI() {
        binding.textHome.visibility = View.GONE  // Hide default text

        // Set listener to reload data when a new month is selected
        binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshData() // Refresh budget and expense data
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        refreshData() // Load data for the initially selected month
    }

    // Populates the month spinner with months retrieved from the database
    private fun setupMonthSpinner() {
        val months = getAvailableMonths()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            months
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.monthSpinner.adapter = adapter
    }

    // Reloads all data displayed on the screen based on the selected month
    private fun refreshData() {
        val selectedMonth = binding.monthSpinner.selectedItem?.toString() ?: return
        updateBudgetDisplay(selectedMonth)
        loadCategoryBudgets(selectedMonth)
        loadMonthlyExpenses(selectedMonth)
    }

    // Updates the monthly budget progress bar and text
    private fun updateBudgetDisplay(month: String) {
        val monthlyBudget = dbHelper.getMonthlyBudget(month)
        val totalExpenses = dbHelper.getTotalExpensesForMonth(month)

        // Set the monthly budget text
        binding.tvMonthlyBudget.text = getString(R.string.monthly_budget, currencyFormat.format(monthlyBudget))

        // Calculate percentage of budget used
        val progress = when {
            monthlyBudget <= 0 -> 0
            totalExpenses >= monthlyBudget -> 100
            else -> (totalExpenses / monthlyBudget * 100).toInt()
        }

        // Update progress bar and progress label
        binding.progressBudget.progress = progress
        binding.tvProgressText.text = getString(R.string.progress_text, progress, currencyFormat.format(totalExpenses))
    }

    // Displays category-wise budget limits and usage in a table
    private fun loadCategoryBudgets(month: String) {
        val table = binding.tableCategoryBudgets
        val rowCount = table.childCount

        // Remove existing rows except header
        if (rowCount > 1) table.removeViews(1, rowCount - 1)

        // Retrieve category data and populate table rows
        val categories = dbHelper.getAllCategoryBudgetsWithUsage(month)
        categories.forEach { (category, limit, used) ->
            TableRow(requireContext()).apply {
                addBudgetRow(category, limit, used)
                background = ContextCompat.getDrawable(context, android.R.drawable.list_selector_background)
            }.also { table.addView(it) }
        }
    }

    // Helper function to add one row of category budget data
    private fun TableRow.addBudgetRow(category: String, limit: Double, used: Double) {
        addTextView(category)
        addTextView(currencyFormat.format(limit))
        addTextView(currencyFormat.format(used))
    }

    // Displays a list of expenses for the selected month in a table
    private fun loadMonthlyExpenses(month: String) {
        val table = binding.tableExpenses
        val rowCount = table.childCount

        // Remove existing rows except header
        if (rowCount > 1) table.removeViews(1, rowCount - 1)

        // Get expense list from the database and populate the table
        dbHelper.getExpensesForMonth(month).forEach { expense ->
            TableRow(requireContext()).apply {
                addTextView(expense.category)
                addTextView(currencyFormat.format(expense.amount))
                addTextView(expenseDateFormat.format(expense.date))
            }.also { table.addView(it) }
        }
    }

    // Adds a centered TextView to a TableRow with common styling
    private fun TableRow.addTextView(text: String) {
        val layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f // Equal weight for all columns
        )
        addView(TextView(context).apply {
            this.text = text
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(CELL_PADDING_DP.dpToPx())
            minHeight = CELL_MIN_HEIGHT_DP.dpToPx()
            this.layoutParams = layoutParams
        })
    }

    // Extension function to convert dp to pixels for consistent layout
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    // Returns a list of months available in the database; fallback to current month if empty
    private fun getAvailableMonths(): List<String> {
        val months = dbHelper.getAllBudgetMonths()
        return if (months.isEmpty()) {
            listOf(monthFormat.format(Date()))
        } else {
            months.sortedDescending()
        }
    }

    // Cleanup when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()  // Close DB connection
        _binding = null   // Release ViewBinding reference
    }
}
