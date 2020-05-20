package com.example.myapplication.SyntaxHighlight.Tokens

data class Token(var type: TokenType, var s:CharSequence, var start: Int = -1, var end: Int = -1) {
    fun toString(str: CharSequence): String {
        return "${this.type}, ${str.subSequence(start, end)}, $start : $end"
    }

    override fun toString(): String {
        return "${this.type}, ${s.subSequence(start, end)}, $start : $end"
    }

    fun getString():String {
        return s.subSequence(start,end).toString()
    }
}
enum class TokenType {
    NUMBER, KEYWORD, COMMENT, MULTILINE_COMMENT, IDENTIFIER, OPERATOR, BRACKETS, PREPROCESSOR, STRING_LITERAL, ERROR
}

