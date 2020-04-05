package com.example.myapplication.SyntaxHighlight.Styler

import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.myapplication.SyntaxHighlight.Highlighter
import com.example.myapplication.SyntaxHighlight.Tokens.Token
import kotlin.math.max
import kotlin.math.min

class GeneralStyler(view: EditText, highlighter: Highlighter, scheme: ColorScheme): Styler(view, highlighter, scheme) {
    override fun styleToken( token: Token) {
        view.text.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(view.context, scheme.getColor(token.type))),
                token.start,
                token.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private var firstColoredLine = -1
    private var lastColoredLine = -1
    private fun getVisibleLines(view: EditText, scrollY: Int):Pair<Int, Int> {
        return Pair(
                view.layout.getLineForVertical(scrollY),
                view.layout.getLineForVertical(scrollY + view.height))
    }

    private fun colorLines(view: EditText, highlighter: Highlighter, firstVisibleLine: Int, lastVisibleLine: Int) {
        val startCharacter = view.layout.getLineStart(firstVisibleLine)
        val endCharacter = view.layout.getLineEnd(lastVisibleLine)
        val tokens = highlighter.tokens
        var iterator = tokens.head
        while (iterator != null && iterator.next != tokens.head) {
            if (iterator.data.start >= startCharacter && iterator.data.end <= endCharacter)
                styleToken(iterator.data)
            iterator = iterator.next
        }
    }

    private fun removeColoring(view: EditText, startLine: Int, endLine: Int) {
        val spansToRemove: Array<Any> = view.text.getSpans(
                view.layout.getLineStart(startLine),
                view.layout.getLineEnd(endLine),
                Any::class.java)
        for (span in spansToRemove) {
            if (span is CharacterStyle) view.text.removeSpan(span)
        }
    }

    override fun resetText() {
        firstColoredLine = -1
        lastColoredLine = -1
    }

    private fun getColorBoundaries(firstVisibleLine: Int, lastVisibleLine: Int): Pair<Int, Int> {
        val lineNumber = lastVisibleLine - firstVisibleLine + 1
        val firstBoundaryLine = max(firstVisibleLine - lineNumber * 2, 0)
        val lastBoundaryLine = min(lastVisibleLine + lineNumber * 2, view.lineCount - 1)
        return Pair(firstBoundaryLine, lastBoundaryLine)
    }
    override fun updateStyling(scrollY: Int) {
//        val lineData =
        var (firstVisibleLine, lastVisibleLine) = getVisibleLines(view, scrollY)
        val (firstBoundaryLine, lastBoundaryLine) = getColorBoundaries(firstVisibleLine, lastVisibleLine)
//        var lastVisibleLine = lineData.second
        if (firstColoredLine == -1) {
            firstColoredLine = firstBoundaryLine
            lastColoredLine = lastBoundaryLine
            colorLines(view, highlighter, firstBoundaryLine, lastBoundaryLine)
            return
        }
        if (firstBoundaryLine < firstColoredLine) {
            colorLines(view, highlighter, firstBoundaryLine, firstColoredLine)
            removeColoring(view, lastBoundaryLine, lastColoredLine)
        } else {
            removeColoring(view, firstColoredLine, firstBoundaryLine)
            colorLines(view, highlighter, lastColoredLine, lastColoredLine)
        }
    }
}