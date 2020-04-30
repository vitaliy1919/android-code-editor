package com.example.myapplication.SyntaxHighlight

import com.example.myapplication.SyntaxHighlight.Tokens.TokenList

abstract class Highlighter {
    var tokens:TokenList = TokenList()
    abstract fun parse(s:CharSequence)
    abstract fun update(s: CharSequence, start: Int, end: Int, offset: Int)
}