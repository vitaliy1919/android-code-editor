package com.example.myapplication.room

import android.net.Uri
import androidx.room.TypeConverter
import com.example.myapplication.history.TextChange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        return gson.toJson(value)
    }

    @TypeConverter
    fun stringToArrayTab(value: String?): ArrayList<TextChange>? {
        if (value == null)
            return ArrayList()
        val gson = Gson()
        val type: Type = object : ArrayList<TextChange?>() {}::class.java
        val res:ArrayList<TextChange>? = gson.fromJson(value, type)
        if (res == null)
            return ArrayList()
        else
            return res
    }
}