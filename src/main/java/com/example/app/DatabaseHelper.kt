package com.example.budget

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// DatabaseHelper class for managing SQLite database operations (creating, upgrading, inserting, reading)
class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Name of the database file
        private const val DATABASE_NAME = "UserDatabase.db"

        // Version of the database (change this if you update the schema)
        private const val DATABASE_VERSION = 1

        // Name of the table that will store user data
        private const val TABLE_NAME = "data"

        // Column names for the user table
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
    }

    // Called when the database is created for the first time
    override fun onCreate(db: SQLiteDatabase?) {
        // SQL query to create the table with columns: id (primary key), username, and password
        val createTableQuery = (
                "CREATE TABLE $TABLE_NAME (" +
                        "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COLUMN_USERNAME TEXT, " +
                        "$COLUMN_PASSWORD TEXT)"
                )
        // Execute the SQL statement to create the table
        db?.execSQL(createTableQuery)
    }

    // Called when the database needs to be upgraded (e.g., version change)
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop the existing table if it exists
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)

        // Recreate the table
        onCreate(db)
    }

    // Inserts a new user into the database
    fun insertUser(username: String, password: String): Long {
        // Prepare values to be inserted using a ContentValues object
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }

        // Get writable database to perform insert operation
        val db = writableDatabase

        // Insert the values into the table and return the result (row ID or -1 if failed)
        return db.insert(TABLE_NAME, null, values)
    }

    // Reads user data and checks if a user exists with the given username and password
    fun readUser(username: String, password: String): Boolean {
        // Get readable database to query user data
        val db = readableDatabase

        // WHERE clause for username and password match
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)

        // Query the table with the given selection
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

        // Check if the cursor has at least one result (user exists)
        val userExists = cursor.count > 0

        // Always close the cursor to avoid memory leaks
        cursor.close()

        // Return whether the user was found
        return userExists
    }
}
