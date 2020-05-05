package com.example.myapplication.SyntaxHighlight.Styler

import com.example.myapplication.R
import com.example.myapplication.SyntaxHighlight.Tokens.TokenType

class GeneralColorScheme : ColorScheme {
    override fun getColor(type: TokenType): Int {
        return when (type) {
            TokenType.NUMBER -> R.color.darkula_number
            TokenType.KEYWORD -> R.color.darkula_keyword
            TokenType.COMMENT -> R.color.darkula_comment
            TokenType.MULTILINE_COMMENT -> R.color.darkula_comment
            TokenType.IDENTIFIER -> R.color.darkula_text
            TokenType.OPERATOR -> R.color.darcula_operator
            TokenType.BRACKETS -> R.color.darkula_bracket
            TokenType.PREPROCESSOR -> R.color.darkula_preprocessor
            TokenType.STRING_LITERAL -> R.color.darkula_string
            TokenType.ERROR -> R.color.darkula_error
        }
    }
}