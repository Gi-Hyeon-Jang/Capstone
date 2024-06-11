package com.example.capstone_1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class UserInputActivity : AppCompatActivity() {
    private val nutritionViewModel: NutritionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main2_layout)

        val weightInput: EditText = findViewById(R.id.weightInput)
        val heightInput: EditText = findViewById(R.id.heightInput)
        val ageInput: EditText = findViewById(R.id.ageInput)
        val genderGroup: RadioGroup = findViewById(R.id.genderGroup)
        val activityLevelGroup: RadioGroup = findViewById(R.id.activityLevelGroup)
        val submitButton: ImageButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            val weight = weightInput.text.toString().toDoubleOrNull() ?: 0.0
            val height = heightInput.text.toString().toDoubleOrNull() ?: 0.0
            val age = ageInput.text.toString().toIntOrNull() ?: 0
            val gender = when (genderGroup.checkedRadioButtonId) {
                R.id.maleRadio -> "male"
                R.id.femaleRadio -> "female"
                else -> "male"
            }
            val activityLevel = when (activityLevelGroup.checkedRadioButtonId) {
                R.id.level1 -> 1.2
                R.id.level2 -> 1.375
                R.id.level3 -> 1.55
                R.id.level4 -> 1.725
                R.id.level5 -> 1.9
                else -> 1.2
            }

            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putFloat("weight", weight.toFloat())
                putFloat("height", height.toFloat())
                putInt("age", age)
                putString("gender", gender)
                putFloat("activity_level", activityLevel.toFloat())
                apply()
            }

            nutritionViewModel.updateNutrition(weight, height, age, gender, activityLevel)

            // Finish the current activity and return to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
