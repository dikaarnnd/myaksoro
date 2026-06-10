package com.dika.myaksoro.data

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class HistoryItem(
    val imagePath: String,
    val cnnOutput: List<String>,
    val chunkResults: List<String>,
    val lstmOutput: String,
    var bitmapCache: Bitmap? = null
)

object HistoryManager {
    private const val PREFS_NAME = "aksoro_history"
    private const val KEY_HISTORY = "history_data"

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        val filename = "history_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    fun saveHistory(context: Context, historyList: List<HistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        historyList.forEach { item ->
            val jsonObj = JSONObject().apply {
                put("imagePath", item.imagePath)
                put("cnnOutput", item.cnnOutput.joinToString(","))
                put("chunkResults", JSONArray(item.chunkResults).toString())
                put("lstmOutput", item.lstmOutput)
            }
            jsonArray.put(jsonObj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun loadHistory(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val historyList = mutableListOf<HistoryItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val imagePath = jsonObj.getString("imagePath")
                val cnnOutputString = jsonObj.getString("cnnOutput")
                val cnnOutput = if (cnnOutputString.isNotEmpty()) cnnOutputString.split(",") else emptyList()
                val lstmOutput = jsonObj.getString("lstmOutput")

                val chunkArrayStr = jsonObj.optString("chunkResults", "[]")
                val chunkArray = JSONArray(chunkArrayStr)
                val chunks = mutableListOf<String>()
                for(j in 0 until chunkArray.length()) {
                    chunks.add(chunkArray.getString(j))
                }

                historyList.add(HistoryItem(imagePath, cnnOutput, chunks, lstmOutput))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return historyList
    }
}