package com.example.myapplication.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.utils.getString
import com.example.myapplication.utils.spToPx

class SettingsData(var mainActivity: MainActivity) {
    var wordWrap = false
    var codeHighlighting = true
    var codeCompletion = true
    var fontSize = 12
    var autoIndent = true


    fun fromSharedPreferenses(pref: SharedPreferences) {
        wordWrap = pref.getBoolean(getString(mainActivity, R.string.wordWrap),false)
        onChangeWordWrap(wordWrap)
        codeHighlighting = pref.getBoolean(getString(mainActivity, R.string.codeHighlighting),true)
        onChangeCodeHighlighting(codeHighlighting)
        codeCompletion = pref.getBoolean(getString(mainActivity, R.string.codeCompletion),true)
        onChangeCodeCompletion(codeCompletion)
        fontSize = pref.getInt(getString(mainActivity, R.string.fontSize),12)
        onChangeFontSize(fontSize)
        autoIndent = pref.getBoolean(getString(mainActivity, R.string.autoIndent),true)
        onChangeAutoIndent(autoIndent)
    }

    private fun onChangeWordWrap(value: Boolean) {
        mainActivity.changeWordWrap(value)

    }

    private fun onChangeCodeHighlighting(value: Boolean) {
        mainActivity.codeEdit.changeSyntaxHighlight(value)
    }

    private fun onChangeCodeCompletion(value: Boolean) {
        mainActivity.codeEdit.changeCodeCompletion(value)
    }

    private fun onChangeFontSize(value: Int) {
        val textSize = spToPx(value.toFloat(), mainActivity)
        mainActivity.codeEdit.setTextSize(value.toFloat())
        mainActivity.numbersView.setTextSize(textSize)
    }

    private fun onChangeAutoIndent(value: Boolean) {

    }


    fun updateValue(key: String, pref: SharedPreferences) {
        when (key) {
            getString(mainActivity, R.string.wordWrap) -> {
                wordWrap = pref.getBoolean(key,false)
                onChangeWordWrap(wordWrap)
            }
            getString(mainActivity, R.string.codeHighlighting) -> {
                codeHighlighting = pref.getBoolean(key,true)
                onChangeCodeHighlighting(codeHighlighting)

            }
            getString(mainActivity, R.string.codeCompletion) -> {
                codeCompletion = pref.getBoolean(key,true)
                onChangeCodeCompletion(codeCompletion)
            }
            getString(mainActivity, R.string.fontSize) -> {
                fontSize = pref.getInt(key,12)
                onChangeFontSize(fontSize)
            }
            getString(mainActivity, R.string.autoIndent) -> {
                autoIndent = pref.getBoolean(key,true)
                onChangeAutoIndent(autoIndent)
            }
        }

    }

}