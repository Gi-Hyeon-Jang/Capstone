package com.example.capstone_1

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class NutritionRepository(context: Context) {

    private val dbHelper = NutritionDatabaseHelper(context)

    fun insertOrUpdateNutrition(date: String, uiState: NutritionUiState) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(NutritionDatabaseHelper.COLUMN_DATE, date)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE, uiState.totalCalorie)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE, uiState.totalCarbohydrate)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS, uiState.totalSugars)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_FAT, uiState.totalFat)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT, uiState.totalSaturatedFat)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN, uiState.totalProtein)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL, uiState.totalCholesterol)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM, uiState.totalSodium)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS, uiState.totalVitamins)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS, uiState.totalMinerals)
        }

        db.insertWithOnConflict(NutritionDatabaseHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getNutritionByDate(date: String): NutritionUiState? {
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.query(
            NutritionDatabaseHelper.TABLE_NAME,
            null,
            "${NutritionDatabaseHelper.COLUMN_DATE} = ?",
            arrayOf(date),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val uiState = NutritionUiState(
                totalCalorie = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE)),
                totalCarbohydrate = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE)),
                totalSugars = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS)),
                totalFat = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_FAT)),
                totalSaturatedFat = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT)),
                totalProtein = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN)),
                totalCholesterol = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL)),
                totalSodium = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM)),
                totalVitamins = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS)),
                totalMinerals = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS))
            )
            cursor.close()
            uiState
        } else {
            cursor.close()
            null
        }
    }
}