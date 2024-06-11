package com.example.capstone_1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NutritionViewModel : ViewModel() {
    private val _nutritionUiState = MutableStateFlow(NutritionUiState())
    val nutritionUiState: StateFlow<NutritionUiState> = _nutritionUiState

    fun updateNutrition(weight: Double, height: Double, age: Int, gender: String, activityLevel: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val intake = calculateDailyIntake(weight, height, age, gender, activityLevel)
            _nutritionUiState.value = intake
        }
    }

    private fun calculateDailyIntake(weight: Double, height: Double, age: Int, gender: String, activityLevel: Double): NutritionUiState {
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

        return NutritionUiState(
            totalCalorie = tdee,
            totalCarbohydrate = carbs,
            totalSugars = sugarLimit,
            totalFat = fat,
            totalSaturatedFat = saturatedFat,
            totalTransFat = transFat,
            totalCholesterol = cholesterol,
            totalSodium = sodium,
            totalVitamins = vitamins,
            totalMinerals = minerals
        )
    }
}
