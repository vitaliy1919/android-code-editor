package com.example.myapplication.SyntaxHighlight.Styler

import android.os.Handler
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


enum class VisibleSectionMovement {
    DONT_INTERSECT, MOVE_UP, MOVE_DOWN
}

class GeneralStyler(view: EditText, highlighter: Highlighter, scheme: ColorScheme): Styler(view, highlighter, scheme) {
    var colored:Boolean = false
    val colorWindowHeight:Int = 500
    val allowableOffset: Int = 100
    var prevTopLine: Int = -1
    var prevBottomLine: Int = -1
    var handler: Handler = Handler(view.context.mainLooper)
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
            if (iterator.data.start >= startCharacter && iterator.data.start <= endCharacter) {
                val data = iterator.data
                handler.post(Runnable {styleToken(data)  })
            }

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
//        val viewHeight
        if (firstColoredLine == -1)
            return true
        if (firstVisibleLine > prevTopLine) {
            // move down
            if (lastColoredLine - lastVisibleLine < colorWindowHeight / 2 - allowableOffset)
                return true
        } else if (firstVisibleLine - firstColoredLine < colorWindowHeight / 2 - allowableOffset)
            return true
        return false
    }
    private fun getColorBoundaries(firstVisibleLine: Int, lastVisibleLine: Int): Pair<Int, Int> {
        val NumberOfLines = lastVisibleLine - firstVisibleLine + 1
        if (firstColoredLine == -1) {
            return Pair(firstVisibleLine, min (lastVisibleLine + colorWindowHeight, view.lineCount - 1))
        } else {
            val middle = (lastVisibleLine + firstVisibleLine) / 2

            val firstLine = middle - colorWindowHeight / 2
            val lastLine  = middle + colorWindowHeight / 2
            val adjustFirst = max(0, firstLine) - firstLine
            val adjustLast = lastLine - min(view.lineCount - 1, lastLine)
            return Pair(firstLine + adjustFirst - adjustLast, lastLine + adjustFirst - adjustLast)
        }
//        if (firstColoredLine == -1) {
////            val firstBoundaryLine = max(firstVisibleLine - lineNumber * 5, 0)
////            val lastBoundaryLine = min(lastVisibleLine + lineNumber * 5, view.lineCount - 1)
//            return Pair(firstVisibleLine, firstVisibleLine + 500)
//        }
//        if (firstVisibleLine > firstColoredLine)
//            return Pair(
//                    max(firstVisibleLine - (firstVisibleLine - firstColoredLine) / 2, 0),
//                    min(lastColoredLine + (firstVisibleLine - firstColoredLine) / 2, view.lineCount - 1))
//        else
//            return Pair(
//                    max(firstColoredLine - (lastColoredLine - lastVisibleLine) / 2, 0),
//                    min(lastVisibleLine + (lastColoredLine - lastVisibleLine) / 2, view.lineCount - 1))
    }
    override fun updateStyling(scrollY: Int, height: Int) {

        val (firstVisibleLine, lastVisibleLine) = getVisibleLines(view, scrollY, height)
        if (!isRefreshNeeded(firstVisibleLine, lastVisibleLine))
            return
        val (firstBoundaryLine, lastBoundaryLine) = getColorBoundaries(firstVisibleLine, lastVisibleLine)
        if (firstColoredLine == -1) {
            colorLines(view, highlighter, firstBoundaryLine, lastBoundaryLine)
        } else if (firstVisibleLine > lastColoredLine || firstColoredLine > lastVisibleLine) {
            // colored area and visible area do not intersect
            removeColoring(view, firstColoredLine, lastColoredLine)
            colorLines(view, highlighter, firstBoundaryLine, lastBoundaryLine)
        } else if (firstBoundaryLine < firstColoredLine) {
            // scrolling up
            colorLines(view, highlighter, firstBoundaryLine, firstColoredLine)
            removeColoring(view, lastBoundaryLine, lastColoredLine)
        } else {
            // scrolling down
            removeColoring(view, firstColoredLine, firstBoundaryLine)
            colorLines(view, highlighter, lastColoredLine, lastBoundaryLine)
        }
        firstColoredLine = firstBoundaryLine
        lastColoredLine = lastBoundaryLine
        prevTopLine = firstVisibleLine
        prevBottomLine = lastVisibleLine
    }
}