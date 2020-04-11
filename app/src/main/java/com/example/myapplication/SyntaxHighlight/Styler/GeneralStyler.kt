package com.example.myapplication.SyntaxHighlight.Styler

import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.myapplication.SyntaxHighlight.Highlighter
import com.example.myapplication.SyntaxHighlight.Tokens.Token
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GeneralStyler(view: EditText, highlighter: Highlighter, scheme: ColorScheme): Styler(view, highlighter, scheme) {
    var colored:Boolean = false
    val windowHeight:Int = 500
    val addLines: Int = 50
    var prevTopLine: Int = -1
    var prevBottomLine: Int = -1
    override fun styleToken( token: Token) {
        view.text.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(view.context, scheme.getColor(token.type))),
                token.start,
                token.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private var firstColoredLine = -1
    private var lastColoredLine = -1
    private fun getVisibleLines(view: EditText, scrollY: Int, height: Int):Pair<Int, Int> {
        return Pair(
                view.layout.getLineForVertical(scrollY),
                view.layout.getLineForVertical(scrollY + height))
    }

    private fun colorLines(view: EditText, highlighter: Highlighter, firstVisibleLine: Int, lastVisibleLine: Int) {
        val startTime = System.currentTimeMillis()
        val startCharacter = view.layout.getLineStart(firstVisibleLine)
        val endCharacter = view.layout.getLineEnd(lastVisibleLine)
        val tokens = highlighter.tokens
        var iterator = tokens.head
        while (iterator != null) {
            if (iterator.data.start >= startCharacter && iterator.data.end <= endCharacter)
                styleToken(iterator.data)
            iterator = iterator.next
            if (iterator == tokens.head)
                break
        }
        Log.d("Coloring","${((System.currentTimeMillis() - startTime) / 1000.0).toString()}s. ${lastVisibleLine - firstVisibleLine} colored")

    }

    private fun removeColoring(view: EditText, startLine: Int, endLine: Int) {
        val startTime = System.currentTimeMillis()
        val begin = view.layout.getLineStart(startLine)
        val end = view.layout.getLineEnd(endLine)
        val spansToRemove: Array<Any> = view.text.getSpans(
                begin,
                end,
                Any::class.java)
        for (span in spansToRemove) {
            if (span is CharacterStyle) {
                if (view.text.getSpanStart(span) >= begin && view.text.getSpanEnd(span) <= end)
                    view.text.removeSpan(span)

            }
        }
        Log.d("Coloring","${((System.currentTimeMillis() - startTime) / 1000.0).toString()}s. ${endLine - startLine} removed")

    }

    override fun resetText() {
        firstColoredLine = -1
        lastColoredLine = -1
    }

    private fun isRefreshNeeded(firstVisibleLine: Int, lastVisibleLine: Int): Boolean {
        return (firstColoredLine == -1 || abs(lastColoredLine - lastVisibleLine) < 499 )
    }
    private fun getColorBoundaries(firstVisibleLine: Int, lastVisibleLine: Int): Pair<Int, Int> {
        val lineNumber = lastVisibleLine - firstVisibleLine + 1
        if (firstColoredLine == -1) {
//            val firstBoundaryLine = max(firstVisibleLine - lineNumber * 5, 0)
//            val lastBoundaryLine = min(lastVisibleLine + lineNumber * 5, view.lineCount - 1)
            return Pair(firstVisibleLine, firstVisibleLine + 500)
        }
        if (firstVisibleLine > firstColoredLine)
            return Pair(
                    max(firstVisibleLine - (firstVisibleLine - firstColoredLine) / 2, 0),
                    min(lastColoredLine + (firstVisibleLine - firstColoredLine) / 2, view.lineCount - 1))
        else
            return Pair(
                    max(firstColoredLine - (lastColoredLine - lastVisibleLine) / 2, 0),
                    min(lastVisibleLine + (lastColoredLine - lastVisibleLine) / 2, view.lineCount - 1))
    }
    override fun updateStyling(scrollY: Int, height: Int) {
//        if (!colored) {
//            colored = true
//            val startTime = System.currentTimeMillis()
//            colorLines(view, highlighter, 0, (view.lineCount - 1) /2)
//            Log.d("Styling",((System.currentTimeMillis() - startTime) / 1000.0).toString())
//
//        }
//        return
//        val lineData =
        var (firstVisibleLine, lastVisibleLine) = getVisibleLines(view, scrollY, height)
        if (!isRefreshNeeded(firstVisibleLine, lastVisibleLine))
            return
        val (firstBoundaryLine, lastBoundaryLine) = getColorBoundaries(firstVisibleLine, lastVisibleLine)
//        var lastVisibleLine = lineData.second
        if (firstColoredLine == -1) {
            firstColoredLine = firstBoundaryLine
            lastColoredLine = lastBoundaryLine
            colorLines(view, highlighter, firstBoundaryLine, lastBoundaryLine)
            return
        }
        if (firstBoundaryLine > lastColoredLine || firstColoredLine > lastBoundaryLine) {
            removeColoring(view, firstColoredLine, lastColoredLine)
            colorLines(view, highlighter, firstBoundaryLine, lastBoundaryLine)
        } else if (firstBoundaryLine < firstColoredLine) {
            colorLines(view, highlighter, firstBoundaryLine, firstColoredLine)
            removeColoring(view, lastBoundaryLine, lastColoredLine)
        } else {
            removeColoring(view, firstColoredLine, firstBoundaryLine)
            colorLines(view, highlighter, lastColoredLine, lastBoundaryLine)
        }
        firstColoredLine = firstBoundaryLine
        lastColoredLine = lastBoundaryLine
    }
}