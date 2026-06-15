package com.example.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class to manage SQLite database for the budget tracking app.
 * It handles the creation and version management of two tables:
 * - category_budget: stores category-specific budget limits.
 * - monthly_budget: stores total budget per month.
 */
class BudgetDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database version, increment if schema changes
        private const val DATABASE_VERSION = 1

        // Name of the SQLite database file
        private const val DATABASE_NAME = "BudgetTracker.db"

        // Table and column names for the category_budget table
        const val TABLE_CATEGORY_BUDGET = "category_budget"
        const val COLUMN_ID = "id"                   // Primary key
        const val COLUMN_CATEGORY = "category"       // Name of the budget category
        const val COLUMN_AMOUNT = "amount"           // Budget amount for the category
        const val COLUMN_TIMESTAMP = "timestamp"     // When the budget was added

        // Table and column names for the monthly_budget table
        const val TABLE_MONTHLY_BUDGET = "monthly_budget"
        const val COLUMN_MONTH = "month"             // Format: MM/yyyy (e.g., 05/2025)
    }

    /**
     * Called when the database is created for the first time.
     * This is where we create tables and initialize data.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // SQL command to create the category_budget table
        val CREATE_CATEGORY_TABLE = """
            CREATE TABLE $TABLE_CATEGORY_BUDGET (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // SQL command to create the monthly_budget table
        val CREATE_MONTHLY_TABLE = """
            CREATE TABLE $TABLE_MONTHLY_BUDGET (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MONTH TEXT NOT NULL UNIQUE,
                $COLUMN_AMOUNT REAL NOT NULL
            )
        """.trimIndent()

        // Execute SQL commands to create both tables
        db.execSQL(CREATE_CATEGORY_TABLE)
        db.execSQL(CREATE_MONTHLY_TABLE)
    }

    /**
     * Called when the database needs to be upgraded.
     * This implementation simply drops existing tables and recreates them.
     * In production, consider proper migration strategy to avoid data loss.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop existing tables if they exist
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORY_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MONTHLY_BUDGET")

        // Recreate tables with updated schema
        onCreate(db)
    }

    /**
     * Adds a new budget entry for a specific category.
     *
     * @param category The name of the category (e.g., "Food", "Transport").
     * @param amount The allocated budget for this category.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    fun addCategoryBudget(category: String, amount: Double): Long {
        val db = this.writableDatabase

        // Prepare values to insert
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, category)
            put(COLUMN_AMOUNT, amount)
        }

        // Insert the new row into the category_budget table
        return db.insert(TABLE_CATEGORY_BUDGET, null, values)
    }

    /**
     * Adds a new monthly budget entry for a given month.
     *
     * @param month The month in format MM/yyyy (e.g., "05/2025").
     * @param amount The total budget for the entire month.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    fun addMonthlyBudget(month: String, amount: Double): Long {
        val db = this.writableDatabase

        // Prepare values to insert
        val values = ContentValues().apply {
            put(COLUMN_MONTH, month)
            put(COLUMN_AMOUNT, amount)
        }

        // Insert the new row into the monthly_budget table
        return db.insert(TABLE_MONTHLY_BUDGET, null, values)
    }
}
