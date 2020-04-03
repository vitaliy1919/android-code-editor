package com.example.myapplication.SyntaxHighlight.Styler

import com.example.myapplication.SyntaxHighlight.Tokens.TokenType

interface ColorScheme {
    fun getColor(type: TokenType) : Int
}