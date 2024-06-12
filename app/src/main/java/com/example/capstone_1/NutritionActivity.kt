package com.example.capstone_1

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NutritionActivity : ComponentActivity() {

    private lateinit var databaseHelper: NutritionDatabaseHelper
    private lateinit var client: OkHttpClient
    private lateinit var calorieEditText: EditText
    private lateinit var carbohydrateEditText: EditText
    private lateinit var sugarsEditText: EditText
    private lateinit var fatEditText: EditText
    private lateinit var saturatedFatEditText: EditText
    private lateinit var proteinEditText: EditText
    private lateinit var cholesterolEditText: EditText
    private lateinit var sodiumEditText: EditText
    private lateinit var vitaminsEditText: EditText
    private lateinit var mineralsEditText: EditText

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToServer(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)
        val intent = intent
        val backButton: ImageButton = findViewById(R.id.imageButton5)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        databaseHelper = NutritionDatabaseHelper(this)
        client = OkHttpClient()

        calorieEditText = findViewById(R.id.calorieEditText)
        carbohydrateEditText = findViewById(R.id.carbohydrateEditText)
        sugarsEditText = findViewById(R.id.sugarsEditText)
        fatEditText = findViewById(R.id.fatEditText)
        saturatedFatEditText = findViewById(R.id.saturatedFatEditText)
        proteinEditText = findViewById(R.id.proteinEditText)
        cholesterolEditText = findViewById(R.id.cholesterolEditText)
        sodiumEditText = findViewById(R.id.sodiumEditText)
        vitaminsEditText = findViewById(R.id.vitaminsEditText)
        mineralsEditText = findViewById(R.id.mineralsEditText)

        val selectImageButton: Button = findViewById(R.id.selectImageButton)
        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        selectImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        saveButton.setOnClickListener {
            saveNutritionData()
        }

        cancelButton.setOnClickListener {
            clearEditTexts()
        }
    }

    private fun uploadImageToServer(uri: Uri) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            val file = File(cacheDir, "upload_image.jpg")

            withContext(Dispatchers.IO) {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            }
            Log.d("NutritionActivity", "Uploading image to server...")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()
            Log.d("NutritionActivity", "Uploading image to server...")

            val request = Request.Builder()
                .url("http://221.139.98.169:5000/food_nutrients")
                .addHeader("Connection", "close")
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            Log.d("NutritionActivity", "JSON Response: $it")
                            val nutrients = parseNutrientsResponse(it)
                            withContext(Dispatchers.Main) {
                                updateEditTexts(nutrients)
                                Toast.makeText(this@NutritionActivity, "업로드 성공", Toast.LENGTH_SHORT).show()
                            }
                        } ?: throw IOException("Unexpected empty response body")
                    } else {
                        throw IOException("Unexpected response code: ${response.code}")
                    }
                } catch (e: IOException) {
                    Log.e("NutritionActivity", "Image upload failed", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@NutritionActivity, "업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun parseNutrientsResponse(response: String): Map<String, String> {
        val jsonObject = JSONObject(response)
        val nutrientsJson = jsonObject.getJSONObject("nutrients")

        val nutrientsMap = mutableMapOf<String, String>()
        val keys = nutrientsJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            nutrientsMap[key] = nutrientsJson.getString(key)
        }
        return nutrientsMap
    }

    private fun updateEditTexts(nutrients: Map<String, String>) {
        calorieEditText.setText(nutrients["칼로리"])
        carbohydrateEditText.setText(nutrients["탄수화물"])
        sugarsEditText.setText(nutrients["당류"])
        fatEditText.setText(nutrients["지방"])
        saturatedFatEditText.setText(nutrients["포화지방"])
        proteinEditText.setText(nutrients["단백질"])
        cholesterolEditText.setText(nutrients["콜레스테롤"])
        sodiumEditText.setText(nutrients["나트륨"])
        vitaminsEditText.setText(nutrients["비타민"])
        mineralsEditText.setText(nutrients["미네랄"])
    }

    private fun saveNutritionData() {
        val db = databaseHelper.writableDatabase
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val existingData = getExistingNutritionData(todayDate)
        val newCalorie = (calorieEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE] ?: 0.0)
        val newCarbohydrate = (carbohydrateEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE] ?: 0.0)
        val newSugars = (sugarsEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS] ?: 0.0)
        val newFat = (fatEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_FAT] ?: 0.0)
        val newSaturatedFat = (saturatedFatEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT] ?: 0.0)
        val newProtein = (proteinEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN] ?: 0.0)
        val newCholesterol = (cholesterolEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL] ?: 0.0)
        val newSodium = (sodiumEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM] ?: 0.0)
        val newVitamins = (vitaminsEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS] ?: 0.0)
        val newMinerals = (mineralsEditText.text.toString().toDoubleOrNull() ?: 0.0) + (existingData[NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS] ?: 0.0)

        val values = ContentValues().apply {
            put(NutritionDatabaseHelper.COLUMN_DATE, todayDate)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE, newCalorie)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE, newCarbohydrate)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS, newSugars)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_FAT, newFat)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT, newSaturatedFat)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN, newProtein)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL, newCholesterol)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM, newSodium)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS, newVitamins)
            put(NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS, newMinerals)
        }

        db.insertWithOnConflict(NutritionDatabaseHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        Toast.makeText(this, "영양소 데이터 저장 성공", Toast.LENGTH_SHORT).show()
    }

    private fun getExistingNutritionData(date: String): Map<String, Double> {
        val db = databaseHelper.readableDatabase
        val cursor = db.query(
            NutritionDatabaseHelper.TABLE_NAME,
            null,
            "${NutritionDatabaseHelper.COLUMN_DATE} = ?",
            arrayOf(date),
            null,
            null,
            null
        )

        val existingData = mutableMapOf(
            NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_FAT to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS to 0.0,
            NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS to 0.0
        )

        cursor.use {
            if (it.moveToFirst()) {
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CALORIE))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CARBOHYDRATE))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SUGARS))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_PROTEIN))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_FAT] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_FAT))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SATURATED_FAT))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_CHOLESTEROL))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_SODIUM))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_VITAMINS))
                existingData[NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS] = it.getDouble(it.getColumnIndexOrThrow(NutritionDatabaseHelper.COLUMN_TOTAL_MINERALS))
            }
        }

        return existingData
    }

    private fun clearEditTexts() {
        calorieEditText.setText("")
        carbohydrateEditText.setText("")
        sugarsEditText.setText("")
        fatEditText.setText("")
        saturatedFatEditText.setText("")
        proteinEditText.setText("")
        cholesterolEditText.setText("")
        sodiumEditText.setText("")
        vitaminsEditText.setText("")
        mineralsEditText.setText("")
    }
}
