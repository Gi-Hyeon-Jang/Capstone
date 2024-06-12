package com.example.capstone_1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var bmiViewModel: BMIViewModel
    private lateinit var nutritionDatabaseHelper: NutritionDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        bmiViewModel = ViewModelProvider(this).get(BMIViewModel::class.java)
        nutritionDatabaseHelper = NutritionDatabaseHelper(this)

        val backButton: ImageButton = findViewById(R.id.goToNutrition)
        backButton.setOnClickListener {
            val intent = Intent(this, NutritionActivity::class.java)
            startActivity(intent)
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val weight = sharedPreferences.getFloat("weight", 0f).toDouble()
        val height = sharedPreferences.getFloat("height", 0f).toDouble()
        val age = sharedPreferences.getInt("age", 0)
        val gender = sharedPreferences.getString("gender", "") ?: ""
        val activityLevel = sharedPreferences.getFloat("activity_level", 0f).toDouble()

        if (weight == 0.0 || height == 0.0 || age == 0 || gender.isEmpty() || activityLevel == 0.0) {
            startActivity(Intent(this, UserInputActivity::class.java))
            finish()
        } else {
            bmiViewModel.updateBMI(weight, height, age, gender, activityLevel)
            bmiViewModel.bmiUiState.observe(this, Observer { state ->
                displayBMIState(state)
                displayNutritionRatios(state)
            })
        }

        setupImageButtonListeners()
    }

    private fun displayBMIState(state: BMIUiState) {
        val df = DecimalFormat("#.#")
        findViewById<TextView>(R.id.textView1).text = df.format(state.BMICalorie)
        findViewById<TextView>(R.id.textView2).text = df.format(state.BMICarbohydrate)
        findViewById<TextView>(R.id.textView3).text = df.format(state.BMISugars)
        findViewById<TextView>(R.id.textView4).text = df.format(state.BMIProtein)
        findViewById<TextView>(R.id.textView5).text = df.format(state.BMIFat)
        findViewById<TextView>(R.id.textView6).text = df.format(state.BMISaturatedFat)
        findViewById<TextView>(R.id.textView7).text = df.format(state.BMICholesterol)
        findViewById<TextView>(R.id.textView8).text = df.format(state.BMISodium)
        findViewById<TextView>(R.id.textView9).text = df.format(state.BMIVitamins)
        findViewById<TextView>(R.id.textView10).text = df.format(state.BMIMinerals)
    }

    private fun displayNutritionRatios(state: BMIUiState) {
        val df = DecimalFormat("#.#")
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = nutritionDatabaseHelper.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM ${NutritionDatabaseHelper.TABLE_NAME} WHERE ${NutritionDatabaseHelper.COLUMN_DATE} = ?", arrayOf(todayDate))

        if (cursor.moveToFirst()) {
            val totalCalorie = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE))
            val totalCarbohydrate = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE))
            val totalSugars = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS))
            val totalFat = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_FAT))
            val totalSaturatedFat = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT))
            val totalProtein = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN))
            val totalCholesterol = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL))
            val totalSodium = cursor.getDouble(cursor.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM))
            findViewById<TextView>(R.id.textView1).text = df.format(totalCalorie)
            findViewById<TextView>(R.id.textView2).text = df.format(totalCalorie / state.BMICalorie * 100)
            findViewById<TextView>(R.id.textView3).text = df.format(totalCarbohydrate)
            findViewById<TextView>(R.id.textView4).text = df.format(totalCarbohydrate / state.BMICarbohydrate * 100)
            findViewById<TextView>(R.id.textView5).text = df.format(totalSugars)
            findViewById<TextView>(R.id.textView6).text = df.format(totalSugars / state.BMISugars * 100)
            findViewById<TextView>(R.id.textView7).text = df.format(totalProtein)
            findViewById<TextView>(R.id.textView8).text = df.format(totalProtein / state.BMIProtein * 100)
            findViewById<TextView>(R.id.textView9).text = df.format(totalFat)
            findViewById<TextView>(R.id.textView10).text = df.format(totalFat / state.BMIFat * 100)
            findViewById<TextView>(R.id.textView11).text = df.format(totalSaturatedFat)
            findViewById<TextView>(R.id.textView12).text = df.format(totalSaturatedFat / state.BMISaturatedFat * 100)
            findViewById<TextView>(R.id.textView13).text = df.format(totalSodium)
            findViewById<TextView>(R.id.textView14).text = df.format(totalSodium / state.BMISodium * 100)
            findViewById<TextView>(R.id.textView15).text = df.format(totalCholesterol)
            findViewById<TextView>(R.id.textView16).text = df.format(totalCholesterol / state.BMICholesterol * 100)

}

        cursor.close()
    }

    private fun setupImageButtonListeners() {
        val imageButton1: ImageButton = findViewById(R.id.imageButton1)
        val imageButton2: ImageButton = findViewById(R.id.imageButton2)
        val imageButton3: ImageButton = findViewById(R.id.imageButton3)
        val imageButton4: ImageButton = findViewById(R.id.imageButton4)

        imageButton1.setOnClickListener {
            val intent = Intent(this, UploadServerActivity::class.java)
            startActivity(intent)
        }

        imageButton2.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        imageButton3.setOnClickListener {
            val intent = Intent(this, CalenderActivity::class.java)
            startActivity(intent)
        }

        imageButton4.setOnClickListener {
            val intent = Intent(this, QueryListActivity::class.java)
            startActivity(intent)
        }
    }
}
