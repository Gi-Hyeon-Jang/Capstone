package com.example.capstone_1

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class QueryListActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QueryListAdapter
    private lateinit var addItemEditText: EditText
    private lateinit var addItemButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_list)

        sharedPreferencesHelper = SharedPreferencesHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        addItemEditText = findViewById(R.id.addItemEditText)
        addItemButton = findViewById(R.id.addItemButton)

        addItemButton.setOnClickListener {
            val newItem = addItemEditText.text.toString()
            if (newItem.isNotEmpty()) {
                addItem(newItem)
                addItemEditText.text.clear()
            } else {
                Toast.makeText(this, "단어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton: ImageButton = findViewById(R.id.buttonBackToMain)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        loadQueries()
    }

    private fun loadQueries() {
        val items = sharedPreferencesHelper.getItems()
        adapter = QueryListAdapter(items) { item ->
            deleteItem(item)
        }
        recyclerView.adapter = adapter
    }

    private fun addItem(word: String) {
        val items = sharedPreferencesHelper.getItems().toMutableList()
        items.add(ResponseItem(word))
        sharedPreferencesHelper.saveItems(items)
        loadQueries()
        Toast.makeText(this, "$word 추가됨", Toast.LENGTH_SHORT).show()
    }

    private fun deleteItem(item: ResponseItem) {
        val items = sharedPreferencesHelper.getItems().toMutableList()
        items.remove(item)
        sharedPreferencesHelper.saveItems(items)
        loadQueries()
        Toast.makeText(this, "${item.word} 삭제됨", Toast.LENGTH_SHORT).show()
    }
}
