package com.example.myapplication.SyntaxHighlight.Tokens

data class Token(var type: TokenType, var s:CharSequence, var start: Int = -1, var end: Int = -1)
enum class TokenType {
    NUMBER, KEYWORD, COMMENT, MULTILINE_COMMENT, IDENTIFIER, OPERATOR, BRACKETS, PREPROCESSOR, STRING_LITERAL
}

