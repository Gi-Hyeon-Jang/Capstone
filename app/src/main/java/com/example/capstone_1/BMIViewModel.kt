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

        // 주요 영양소 (g 단위)
        val carbs = tdee * 0.55 / 4 // 탄수화물 칼로리를 그램으로 변환 (1g = 4kcal)
        val protein = tdee * 0.2 / 4 // 단백질 칼로리를 그램으로 변환 (1g = 4kcal)
        val fat = tdee * 0.25 / 9 // 지방 칼로리를 그램으로 변환 (1g = 9kcal)
        val sugars = tdee * 0.1 / 4 // 당류 칼로리를 그램으로 변환 (1g = 4kcal)
        val saturatedFat = fat * 0.1 // 포화지방은 총 지방의 10%
        val transFat = fat * 0.01 // 트랜스지방은 총 지방의 1%

        // 기타 영양소 (mg 단위)
        val cholesterol = 300.0 // 권장 일일 섭취량 mg
        val sodium = 2300.0 // 권장 일일 섭취량 mg
        val vitamins = 100.0 // Placeholder value
        val minerals = 100.0 // Placeholder value

        return BMIUiState(
            BMICalorie = tdee,
            BMICarbohydrate = carbs,
            BMISugars = sugars,
            BMIProtein = protein,
            BMIFat = fat,
            BMISaturatedFat = saturatedFat,
            BMICholesterol = cholesterol,
            BMISodium = sodium,
            BMIVitamins = vitamins,
            BMIMinerals = minerals
        )
    }
}
