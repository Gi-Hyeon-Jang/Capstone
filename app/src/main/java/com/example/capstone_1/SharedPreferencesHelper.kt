package com.example.capstone_1

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("response_items", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addItems(items: List<ResponseItem>) {
        val currentItems = getItems().toMutableList()
        items.forEach { newItem ->
            if (currentItems.none { it.word == newItem.word }) {
                currentItems.add(newItem)
            }
        }
        saveItems(currentItems)
    }

    fun getItems(): List<ResponseItem> {
        val json = sharedPreferences.getString("items", null) ?: return emptyList()
        val type = object : TypeToken<List<ResponseItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveItems(items: List<ResponseItem>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(items)
        editor.putString("items", json)
        editor.apply()
    }
}
