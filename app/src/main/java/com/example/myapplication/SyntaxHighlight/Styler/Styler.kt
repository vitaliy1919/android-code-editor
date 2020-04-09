package com.example.myapplication.SyntaxHighlight.Styler

import android.text.Spannable
import android.widget.EditText
import com.example.myapplication.SyntaxHighlight.Highlighter
import com.example.myapplication.SyntaxHighlight.Tokens.Token

abstract class Styler(var view: EditText, var highlighter: Highlighter, var scheme: ColorScheme) {
    abstract fun updateStyling(scrollY: Int, height: Int)
    abstract fun styleToken(token: Token)
    abstract fun resetText()
}