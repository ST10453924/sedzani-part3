package com.example.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "BudgetTracker.db"
        const val DATABASE_VERSION = 1

        // Monthly Budget Table
        const val TABLE_MONTHLY_BUDGET = "monthly_budget"
        const val COL_MONTH = "month"
        const val COL_BUDGET = "budget"

        // Category Budget Table
        const val TABLE_CATEGORY_BUDGET = "category_budget"
        const val COL_CATEGORY = "category"
        const val COL_BUDGET_LIMIT = "budget_limit"

        // Expenses Table
        const val TABLE_EXPENSES = "expenses"
        const val COL_ID = "_id"
        const val COL_AMOUNT = "amount"
        const val COL_DATE = "date"
        const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON;")

        db.execSQL("""
            CREATE TABLE $TABLE_MONTHLY_BUDGET (
                $COL_MONTH TEXT PRIMARY KEY,
                $COL_BUDGET REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORY_BUDGET (
                $COL_CATEGORY TEXT PRIMARY KEY,
                $COL_BUDGET_LIMIT REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_EXPENSES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORY TEXT,
                $COL_AMOUNT REAL NOT NULL,
                $COL_DATE TEXT NOT NULL,
                FOREIGN KEY($COL_CATEGORY) 
                REFERENCES $TABLE_CATEGORY_BUDGET($COL_CATEGORY)
                ON DELETE CASCADE
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORY_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MONTHLY_BUDGET")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // region Monthly Budget Operations
    fun addMonthlyBudget(month: String, budget: Double): Long {
        val values = contentValuesOf(
            COL_MONTH to month,
            COL_BUDGET to budget
        )
        return writableDatabase.insert(TABLE_MONTHLY_BUDGET, null, values)
    }

    fun getMonthlyBudget(month: String): Double {
        val cursor = readableDatabase.query(
            TABLE_MONTHLY_BUDGET,
            arrayOf(COL_BUDGET),
            "$COL_MONTH = ?",
            arrayOf(month),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }
    // endregion

    // region Category Budget Operations
    fun addCategoryBudget(category: String, limit: Double): Long {
        val values = contentValuesOf(
            COL_CATEGORY to category,
            COL_BUDGET_LIMIT to limit
        )
        return writableDatabase.insert(TABLE_CATEGORY_BUDGET, null, values)
    }

    fun getAllCategoryBudgets(): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        val cursor = readableDatabase.query(
            TABLE_CATEGORY_BUDGET,
            arrayOf(COL_CATEGORY, COL_BUDGET_LIMIT),
            null, null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                val category = it.getString(0)
                val limit = it.getDouble(1)
                map[category] = limit
            }
        }
        return map
    }
    // endregion

    // region Expense Operations
    fun addExpense(category: String, amount: Double, date: Date = Date()): Long {
        val values = contentValuesOf(
            COL_CATEGORY to category,
            COL_AMOUNT to amount,
            COL_DATE to dateFormat.format(date)
        )
        return writableDatabase.insert(TABLE_EXPENSES, null, values)
    }

    fun getExpensesForMonth(month: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val cursor = readableDatabase.rawQuery("""
            SELECT $COL_CATEGORY, $COL_AMOUNT, $COL_DATE FROM $TABLE_EXPENSES
            WHERE strftime('%m/%Y', $COL_DATE) = ?
        """.trimIndent(), arrayOf(month))

        cursor.use {
            while (it.moveToNext()) {
                val category = it.getString(0)
                val amount = it.getDouble(1)
                val dateStr = it.getString(2)
                val date = try {
                    dateFormat.parse(dateStr)
                } catch (e: Exception) {
                    null
                }
                if (date != null) {
                    expenses.add(Expense(category, amount, date))
                }
            }
        }
        return expenses
    }

    fun getTotalExpensesForMonth(month: String): Double {
        val cursor = readableDatabase.rawQuery("""
            SELECT SUM($COL_AMOUNT) FROM $TABLE_EXPENSES 
            WHERE strftime('%m/%Y', $COL_DATE) = ?
        """.trimIndent(), arrayOf(month))
        return cursor.use {
            if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }
    // endregion

    // region Combined Operations
    fun getAllCategoryBudgetsWithUsage(month: String): List<Triple<String, Double, Double>> {
        val result = mutableListOf<Triple<String, Double, Double>>()
        val limits = getAllCategoryBudgets()
        val expenses = getExpensesForMonth(month).groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        limits.forEach { (category, limit) ->
            val used = expenses[category] ?: 0.0
            result.add(Triple(category, limit, used))
        }

        return result
    }

    fun getAllBudgetMonths(): List<String> {
        val months = mutableListOf<String>()
        val cursor = readableDatabase.query(
            TABLE_MONTHLY_BUDGET,
            arrayOf(COL_MONTH),
            null, null, null, null,
            "$COL_MONTH DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                months.add(it.getString(0))
            }
        }
        return months
    }
    // endregion

    private fun contentValuesOf(vararg pairs: Pair<String, Any?>): ContentValues {
        val cv = ContentValues()
        for ((key, value) in pairs) {
            when (value) {
                is String -> cv.put(key, value)
                is Double -> cv.put(key, value)
                is Int -> cv.put(key, value)
                is Long -> cv.put(key, value)
                is Boolean -> cv.put(key, value)
                is Date -> cv.put(key, dateFormat.format(value))
                null -> cv.putNull(key)
            }
        }
        return cv
    }

    data class Expense(
        val category: String,
        val amount: Double,
        val date: Date
    )
}
