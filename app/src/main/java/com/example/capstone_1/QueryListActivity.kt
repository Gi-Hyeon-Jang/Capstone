package com.example.capstone_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class QueryListActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QueryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_list)

        sharedPreferencesHelper = SharedPreferencesHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadQueries()
    }

    private fun loadQueries() {
        val items = sharedPreferencesHelper.getItems()
        adapter = QueryListAdapter(items) { item ->
            deleteItem(item)
        }
        recyclerView.adapter = adapter
    }

    private fun deleteItem(item: ResponseItem) {
        val items = sharedPreferencesHelper.getItems().toMutableList()
        items.remove(item)
        sharedPreferencesHelper.saveItems(items)
        loadQueries()
        Toast.makeText(this, "${item.word} 삭제됨", Toast.LENGTH_SHORT).show()
    }
}
