package com.example.myapplication.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.R

class SettingsData() {
    var wordWrap = false
    var codeHighlighting = true
    var codeCompletion = true
    var fontSize = 12
    var autoIndent = true

    fun getString(context: Context, id: Int): String {
        return context.resources.getString(id)
    }

    fun fromSharedPreferenses(context: Context, pref: SharedPreferences) {
        wordWrap = pref.getBoolean(getString(context, R.string.wordWrap),false)
        codeHighlighting = pref.getBoolean(getString(context, R.string.codeHighlighting),true)
        codeCompletion = pref.getBoolean(getString(context, R.string.codeCompletion),true)
        fontSize = pref.getInt(getString(context, R.string.fontSize),12)
        autoIndent = pref.getBoolean(getString(context, R.string.autoIndent),true)
    }

    fun updateValue(key: String, context: Context,  pref: SharedPreferences) {
        when (key) {
            getString(context, R.string.wordWrap) -> {
                wordWrap = pref.getBoolean(getString(context, R.string.wordWrap),false)
            }
            getString(context, R.string.codeHighlighting) -> {
                codeHighlighting = pref.getBoolean(getString(context, R.string.codeHighlighting),true)

            }
            getString(context, R.string.codeCompletion) -> {
                codeCompletion = pref.getBoolean(getString(context, R.string.codeCompletion),true)

            }
            getString(context, R.string.fontSize) -> {
                fontSize = pref.getInt(getString(context, R.string.fontSize),12)

            }
            getString(context, R.string.autoIndent) -> {
                autoIndent = pref.getBoolean(getString(context, R.string.autoIndent),true)
            }
        }

    }

}