package com.example.capstone_1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private lateinit var bmiViewModel: BMIViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        bmiViewModel = ViewModelProvider(this).get(BMIViewModel::class.java)

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
           //    findViewById<TextView>(R.id.textView1).text = state.BMICalorie.toString()
            //    findViewById<TextView>(R.id.textView2).text = state.BMICarbohydrate.toString()
            //    findViewById<TextView>(R.id.textView3).text = state.BMISugars.toString()
            //    findViewById<TextView>(R.id.textView4).text = state.BMIFat.toString()
             //   findViewById<TextView>(R.id.textView5).text = state.BMISaturatedFat.toString()
             //   findViewById<TextView>(R.id.textView6).text = state.BMITransFat.toString()
                findViewById<TextView>(R.id.textView7).text = state.BMICholesterol.toString()
                findViewById<TextView>(R.id.textView8).text = state.BMISodium.toString()
                findViewById<TextView>(R.id.textView9).text = state.BMIVitamins.toString()
                findViewById<TextView>(R.id.textView10).text = state.BMIMinerals.toString()
                findViewById<TextView>(R.id.textView11).text = state.BMICalorie.toString()
                findViewById<TextView>(R.id.textView12).text = state.BMICarbohydrate.toString()
                findViewById<TextView>(R.id.textView13).text = state.BMISugars.toString()
                findViewById<TextView>(R.id.textView14).text = state.BMIFat.toString()
                findViewById<TextView>(R.id.textView15).text = state.BMISaturatedFat.toString()
                findViewById<TextView>(R.id.textView16).text = state.BMITransFat.toString()
            })
        }
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
