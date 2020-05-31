package com.example.myapplication.room

import android.net.Uri
import android.util.Log
import androidx.room.TypeConverter
import com.example.myapplication.history.TextChange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.StringBuilder
import java.lang.reflect.Type


class Converters {
    @TypeConverter
    fun uriToString(value: Uri?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun stringToUri(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @TypeConverter
    fun arrayTabToString(value: ArrayList<TextChange>?): String? {
        if (value == null)
            return null
        val gson = Gson()
        Log.d("ArrayValue", "${value.toString()}\nResult: ${gson.toJson(value)}")
        return gson.toJson(value)
    }

    @TypeConverter
    fun stringToArrayTab(value: String?): ArrayList<TextChange>? {
        if (value == null)
            return ArrayList()
        val gson = Gson()
//        val type: Type = object : ArrayList<TextChange?>() {}::class.java
        val res:Array<TextChange>? = gson.fromJson(value, Array<TextChange>::class.java)
        Log.d("ArrayValue", "${value}")

        if (res == null) {
            Log.d("ArrayValue", "Result is null")
            return ArrayList()
        }
        if (res.size == 0) {
            Log.d("ArrayValue", "Result is empty")

        }
        for (r in res) {
            Log.d("ArrayValue", "${r.start}, ${r.oldText}, ${r.newText}")
        }

        return res.toCollection(ArrayList())
    }
}