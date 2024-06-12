package com.example.capstone_1
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NutritionDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_DATE TEXT PRIMARY KEY," +
                    "$COLUMN_TOTAL_CALORIE REAL," +
                    "$COLUMN_TOTAL_CARBOHYDRATE REAL," +
                    "$COLUMN_TOTAL_SUGARS REAL," +
                    "$COLUMN_TOTAL_FAT REAL," +
                    "$COLUMN_TOTAL_SATURATED_FAT REAL," +
                    "$COLUMN_TOTAL_PROTEIN REAL," +
                    "$COLUMN_TOTAL_CHOLESTEROL REAL," +
                    "$COLUMN_TOTAL_SODIUM REAL," +
                    "$COLUMN_TOTAL_VITAMINS REAL," +
                    "$COLUMN_TOTAL_MINERALS REAL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "nutrition.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "nutrition"
        const val COLUMN_DATE = "date"
        const val COLUMN_TOTAL_CALORIE = "total_calorie"
        const val COLUMN_TOTAL_CARBOHYDRATE = "total_carbohydrate"
        const val COLUMN_TOTAL_SUGARS = "total_sugars"
        const val COLUMN_TOTAL_FAT = "total_fat"
        const val COLUMN_TOTAL_SATURATED_FAT = "total_saturated_fat"
        const val COLUMN_TOTAL_PROTEIN = "total_trans_fat"
        const val COLUMN_TOTAL_CHOLESTEROL = "total_cholesterol"
        const val COLUMN_TOTAL_SODIUM = "total_sodium"
        const val COLUMN_TOTAL_VITAMINS = "total_vitamins"
        const val COLUMN_TOTAL_MINERALS = "total_minerals"
    }
}