package com.example.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLiteOpenHelper class for managing the local Expense Tracker database.
 * This class is responsible for creating and managing a table that stores expense records,
 * including fields such as category, title, amount, date, description, and an optional image path.
 */
class ExpenseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // The version number of the database. Increment this when the schema is changed.
        private const val DATABASE_VERSION = 1

        // The name of the database file.
        private const val DATABASE_NAME = "ExpenseTracker.db"

        // Table name that stores the expense records.
        const val TABLE_NAME = "expenses"

        // Column names for the 'expenses' table.
        const val COLUMN_ID = "id"                       // Unique ID for each expense (Primary Key)
        const val COLUMN_CATEGORY = "category"           // Expense category (e.g., Food, Transport)
        const val COLUMN_TITLE = "title"                 // Short title or label for the expense
        const val COLUMN_AMOUNT = "amount"               // Expense amount
        const val COLUMN_DATE = "date"                   // Date of the expense (in String format)
        const val COLUMN_DESCRIPTION = "description"     // Detailed description of the expense
        const val COLUMN_IMAGE_PATH = "image_path"       // Optional path to an image/receipt (if any)
    }

    /**
     * Called when the database is created for the first time.
     * This method defines the structure (schema) of the 'expenses' table.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // SQL command to create the expenses table with required columns
        val CREATE_TABLE = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"   // Auto-incrementing ID
                + "$COLUMN_CATEGORY TEXT NOT NULL,"                 // Category must be provided
                + "$COLUMN_TITLE TEXT NOT NULL,"                    // Title must be provided
                + "$COLUMN_AMOUNT REAL NOT NULL,"                   // Amount must be provided
                + "$COLUMN_DATE TEXT NOT NULL,"                     // Date must be provided
                + "$COLUMN_DESCRIPTION TEXT NOT NULL,"              // Description must be provided
                + "$COLUMN_IMAGE_PATH TEXT)")                       // Image path is optional

        // Execute the SQL command to create the table
        db.execSQL(CREATE_TABLE)
    }

    /**
     * Called when the database needs to be upgraded.
     * For now, it simply drops the existing table and recreates it.
     * NOTE: This will delete existing data; a migration strategy should be used in production.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Remove the old version of the table if it exists
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")

        // Recreate the table using the updated schema
        onCreate(db)
    }

    /**
     * Adds a new expense record to the database.
     *
     * @param category The category of the expense (e.g., Food, Bills).
     * @param title A brief title/label for the expense (e.g., "Lunch").
     * @param amount The amount spent.
     * @param date The date the expense occurred (stored as String, e.g., "2025-05-12").
     * @param description A detailed description of the expense.
     * @param imagePath (Optional) A file path to an image or receipt for the expense.
     *
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    fun addExpense(
        category: String,
        title: String,
        amount: Double,
        date: String,
        description: String,
        imagePath: String?
    ): Long {
        // Open the database in write mode
        val db = this.writableDatabase

        // Create a map of values, where column names are keys
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, category)
            put(COLUMN_TITLE, title)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_DATE, date)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_IMAGE_PATH, imagePath) // Can be null
        }

        // Insert the new row into the expenses table
        val id = db.insert(TABLE_NAME, null, values)

        // Close the database connection to free up resources
        db.close()

        // Return the new record's ID (or -1 if the insertion failed)
        return id
    }
}
