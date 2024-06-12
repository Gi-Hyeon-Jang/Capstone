package com.example.capstone_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
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
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton // Change Button to ImageButton
    private lateinit var searchResultsListView: ListView
    private var selectedResult: String? = null

    private val client = OkHttpClient()
    private val serverUrl = "http://221.139.98.169:5000/food_name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton) // No need to change this line
        searchResultsListView = findViewById(R.id.searchResultsListView)
        val backButton: ImageButton = findViewById(R.id.buttonBackToMain)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                searchFoodName(query)
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        searchResultsListView.setOnItemClickListener { parent, view, position, id ->
            selectedResult = parent.getItemAtPosition(position) as String
            Toast.makeText(this, "선택된 항목: $selectedResult", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UploadServerActivity::class.java).apply {
                putExtra("selectedResult", selectedResult)
            }
            startActivity(intent)
        }
    }
    private fun searchFoodName(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = "{\"name\":\"$query\"}"
                val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(serverUrl)
                    .addHeader("Connection", "close")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response -> // use 'use' function to automatically close the response body
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
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "Exception during search", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SearchActivity, "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseJsonResponse(json: String): List<String> {
        val jsonArray = JSONArray(json)
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
