package com.example.capstone_1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BMIViewModel : ViewModel() {
    private val _bmiUiState = MutableLiveData<BMIUiState>()
    val bmiUiState: LiveData<BMIUiState> = _bmiUiState

    fun updateBMI(weight: Double, height: Double, age: Int, gender: String, activityLevel: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val intake = calculateDailyIntake(weight, height, age, gender, activityLevel)
            _bmiUiState.postValue(intake)
        }
    }

    private fun calculateDailyIntake(weight: Double, height: Double, age: Int, gender: String, activityLevel: Double): BMIUiState {
        val bmr = if (gender.lowercase() == "male") {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        }

        val tdee = bmr * activityLevel

        val carbCalories = tdee * 0.55
        val proteinCalories = tdee * 0.2
        val fatCalories = tdee * 0.25
        val sugarLimit = tdee * 0.1

        val carbs = carbCalories / 4
        val protein = proteinCalories / 4
        val fat = fatCalories / 9
        val saturatedFat = fat * 0.1
        val transFat = fat * 0.01
        val cholesterol = 300.0 // Recommended daily intake in mg
        val sodium = 2300.0 // Recommended daily intake in mg
        val vitamins = 100.0 // Placeholder value, actual values depend on specific vitamins
        val minerals = 100.0 // Placeholder value, actual values depend on specific minerals

        return BMIUiState(
            BMICalorie = tdee,
            BMICarbohydrate = carbs,
            BMISugars = sugarLimit,
            BMIFat = fat,
            BMISaturatedFat = saturatedFat,
            BMITransFat = transFat,
            BMICholesterol = cholesterol,
            BMISodium = sodium,
            BMIVitamins = vitamins,
            BMIMinerals = minerals
        )
    }
}
