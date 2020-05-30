package com.example.myapplication.SyntaxHighlight

import com.example.myapplication.SyntaxHighlight.Tokens.BracketToken
import com.example.myapplication.SyntaxHighlight.Tokens.Token
import com.example.myapplication.SyntaxHighlight.Tokens.TokenList

abstract class Highlighter {
    @Volatile
    var isResultReady: Boolean = false
    var tokens:ArrayList<Token> = ArrayList()
    abstract fun brackets(): ArrayList<BracketToken>

    abstract fun identifiers(): HashSet<String>
    abstract fun parse(s:CharSequence)
    abstract fun update(s: CharSequence, start: Int, end: Int, offset: Int, cursor: Int)
}