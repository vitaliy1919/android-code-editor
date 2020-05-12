package com.example.myapplication.views.Tokenizer

import android.widget.MultiAutoCompleteTextView
import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter
import kotlin.math.max
import kotlin.math.min

class CPlusPlusTokenizer : MultiAutoCompleteTextView.Tokenizer {
    private fun partOfToken(c: Char): Boolean {
//        return !(operators.indexOf(c) != -1 || c.isWhitespace() || parentheses.indexOf(c) != -1)
        return c == '_' || c.isLetterOrDigit()
    }
    override fun findTokenEnd(text: CharSequence?, cursor: Int): Int {
        if (text == null)
            return 0
        var iter = cursor + 1
        while (iter < text.length && partOfToken(text[iter]))
            iter++
        if (iter < text.length)
            iter--
        return min(iter, text.length - 1)
    }

    override fun findTokenStart(text: CharSequence?, cursor: Int): Int {
        if (text == null)
            return 0
        var iter = cursor - 1
        while (iter >= 0 && partOfToken(text[iter]))
            iter--
        if (iter >= 0)
            iter++
        return max(iter, 0)
    }

    override fun terminateToken(text: CharSequence?): CharSequence {
        return text ?: ""
    }
}