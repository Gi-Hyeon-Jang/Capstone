package com.example.capstone_1

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class UploadServerActivity : ComponentActivity() {

    private var imageUri: Uri? = null
    private var selectedOption: Int = 1
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private var extractFoodResult: List<String> = emptyList()
    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var resultAdapter: ResultAdapter

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_server)

        sharedPreferencesHelper = SharedPreferencesHelper(this)

        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedOption = when (checkedId) {
                R.id.radioButton1 -> 1 // 처방전 입력
                R.id.radioButton2 -> 2 // 음식 검사하기
                else -> 1
            }
        }
        val backButton: Button = findViewById(R.id.buttonBackToMain)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val uploadButton: Button = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        val sendButton: Button = findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            imageUri?.let { uri ->
                uploadImageToServer(uri, selectedOption)
            }
        }

        val checkButton: Button = findViewById(R.id.checkButton)
        checkButton.setOnClickListener {
            checkResults()
        }

        resultRecyclerView = findViewById(R.id.resultRecyclerView)
        resultRecyclerView.layoutManager = LinearLayoutManager(this)
        resultAdapter = ResultAdapter(emptyList())
        resultRecyclerView.adapter = resultAdapter

        // SearchActivity에서 넘어온 인텐트를 처리합니다.
        handleIntent()
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

    private fun uploadImageToServer(uri: Uri, option: Int) {
        val client = createOkHttpClient()

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

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val url = when (option) {
                1 -> "http://221.139.98.169:5000/drug_text" // 처방전 입력
                2 -> "http://221.139.98.169:5000/extract_food" // 음식 검사하기
                else -> "http://221.139.98.169:5000/extract_food"
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .post(requestBody)
                .build()

            retryRequest(client, request, 3) { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        when (option) {
                            1 -> {
                                val responseItems = parseDrugTextResponse(it)
                                sharedPreferencesHelper.addItems(responseItems)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@UploadServerActivity, "업로드 및 저장 성공", Toast.LENGTH_SHORT).show()
                                }
                            }
                            2 -> {
                                extractFoodResult = parseExtractFoodResponse(it)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@UploadServerActivity, "음식 검사 성공", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UploadServerActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun retryRequest(client: OkHttpClient, request: Request, retries: Int, onSuccess: suspend (okhttp3.Response) -> Unit) {
        var attempt = 0
        while (attempt < retries) {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                if (response.isSuccessful) {
                    onSuccess(response)
                    return
                } else {
                    attempt++
                }
            } catch (e: Exception) {
                attempt++
                if (attempt >= retries) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UploadServerActivity, "업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun parseDrugTextResponse(json: String): List<ResponseItem> {
        val jsonObject = JSONObject(json)
        val matches = jsonObject.getString("matches")
        val jsonArray = JSONArray(matches)
        val items = mutableListOf<ResponseItem>()
        for (i in 0 until jsonArray.length()) {
            val word = jsonArray.getString(i)
            items.add(ResponseItem(word))
        }
        return items
    }

    private fun parseExtractFoodResponse(json: String): List<String> {
        val jsonObject = JSONObject(json)
        val matches = jsonObject.getString("matches")
        val jsonArray = JSONArray(matches)
        val items = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            items.add(jsonArray.getString(i))
        }
        return items
    }

    private fun checkResults() {
        val drugList = sharedPreferencesHelper.getItems().map { it.word }
        val foodList = extractFoodResult

        val client = createOkHttpClient()
        val json = JSONObject().apply {
            put("drug", JSONArray(drugList))
            put("food", JSONArray(foodList))
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://221.139.98.169:5000/drug_food")
            .addHeader("Connection", "close")
            .post(requestBody)
            .build()

        lifecycleScope.launch {
            retryRequest(client, request, 3) { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val results = parseJsonResponse(it)
                        withContext(Dispatchers.Main) {
                            resultAdapter.updateData(results)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UploadServerActivity, "결과 조회 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun parseJsonResponse(json: String): List<String> {
        val jsonArray = JSONArray(json)
        val items = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            items.add(jsonArray.getString(i))
        }
        return items
    }

    private fun handleIntent() {
        val selectedResult = intent.getStringExtra("selectedResult")
        selectedResult?.let {
            // selectedResult 값을 사용하여 필요한 작업 수행
            // 예: 업로드 함수 호출 등
            uploadImageToServer(Uri.parse(it), 2) // 예시: selectedResult를 사용하여 URI로 변환 후 업로드 호출
        }
    }
}
