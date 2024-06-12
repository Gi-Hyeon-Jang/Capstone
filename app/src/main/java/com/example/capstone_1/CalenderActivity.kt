package com.example.capstone_1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone_1.databinding.CalenderLayoutBinding
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

class CalenderActivity : AppCompatActivity() {

    private lateinit var binding: CalenderLayoutBinding
    private lateinit var repository: NutritionRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CalenderLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val backButton: ImageButton = findViewById(R.id.buttonBackToMain)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        repository = NutritionRepository(this)

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = dateFormat.format(GregorianCalendar(year, month, dayOfMonth).time)
            loadNutritionData(date)
        }
    }

    private fun loadNutritionData(date: String) {
        val uiState = repository.getNutritionByDate(date)

        uiState?.let {
            binding.totalCalorieText.text = it.totalCalorie.toString()
            binding.totalCarbohydrateText.text = it.totalCarbohydrate.toString()
            binding.totalSugarsText.text = it.totalSugars.toString()
            binding.totalFatText.text = it.totalFat.toString()
            binding.totalSaturatedFatText.text = it.totalSaturatedFat.toString()
            binding.totalTransFatText.text = it.totalProtein.toString()
            binding.totalCholesterolText.text = it.totalCholesterol.toString()
            binding.totalSodiumText.text = it.totalSodium.toString()
            binding.totalVitaminsText.text = it.totalVitamins.toString()
            binding.totalMineralsText.text = it.totalMinerals.toString()
        } ?: run {
            // 데이터가 없을 경우 빈 문자열로 설정
            binding.totalCalorieText.text = ""
            binding.totalCarbohydrateText.text = ""
            binding.totalSugarsText.text = ""
            binding.totalFatText.text = ""
            binding.totalSaturatedFatText.text = ""
            binding.totalTransFatText.text = ""
            binding.totalCholesterolText.text = ""
            binding.totalSodiumText.text = ""
            binding.totalVitaminsText.text = ""
            binding.totalMineralsText.text = ""
        }
    }
}
