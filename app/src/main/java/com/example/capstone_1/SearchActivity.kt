package com.example.capstone_1

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var searchResultsListView: ListView
    private var selectedResult: String? = null

    private val client = createOkHttpClient()
    private val serverUrl = "http://221.139.98.169:5000/food_name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        searchResultsListView = findViewById(R.id.searchResultsListView)

        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                searchFoodName(query)
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }
        val backButton: Button = findViewById(R.id.buttonBackToMain)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        searchResultsListView.setOnItemClickListener { parent, view, position, id ->
            selectedResult = parent.getItemAtPosition(position) as String
            Toast.makeText(this, "선택된 항목: $selectedResult", Toast.LENGTH_SHORT).show()
        }

        // 새 버튼을 추가하여 UploadServerActivity로 이동
        val uploadButton: Button = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            selectedResult?.let { result ->
                val intent = Intent(this, UploadServerActivity::class.java)
                intent.putExtra("selectedResult", result)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "먼저 항목을 선택하세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun searchFoodName(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("name", query)
                }
                val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
                val request = Request.Builder()
                    .url(serverUrl)
                    .addHeader("Connection", "close")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val results = parseJsonResponse(it)
                        withContext(Dispatchers.Main) {
                            displayResults(results)
                        }
                    } ?: throw Exception("Response body is null")
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SearchActivity, "검색 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SearchActivity, "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseJsonResponse(json: String): List<String> {
        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject.getJSONArray("names")
        val results = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            results.add(jsonArray.getString(i))
        }
        return results
    }

    private fun displayResults(results: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results)
        searchResultsListView.adapter = adapter
    }
}
