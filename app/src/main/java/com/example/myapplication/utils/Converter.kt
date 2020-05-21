package com.example.myapplication.utils

import android.content.Context
import android.util.TypedValue
import android.widget.EditText
import android.widget.TextView
import com.example.myapplication.SyntaxHighlight.Tokens.Token
import kotlin.math.max
import kotlin.math.min

enum class InterSectionResult {
    INTERSECTS, OUTSIDE_LEFT, OUTSIDE_RIGHT
}

fun intersect(segmentAStart: Int, segmentAEnd: Int, segmentBStart: Int, segmentBEnd: Int): InterSectionResult {
    if (segmentAEnd <= segmentBStart)
        return InterSectionResult.OUTSIDE_LEFT
    if (segmentBEnd <= segmentAStart)
        return InterSectionResult.OUTSIDE_RIGHT
    return InterSectionResult.INTERSECTS
}

fun spToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}
fun dpToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, context.resources.displayMetrics)
}

fun toString(a: ArrayList<Token>):String {
    var s = "[\n"
    for (i in a) {
        s += i.toString() + "\n"
    }
    s += "]"
    return s
}
fun findCharBefore(s: CharSequence, index: Int, c: Char): Int {
    if (s.isEmpty())
        return 0
    var pos = index
    while (pos >= 0 && s[pos] != c)
        pos--
    return max(pos, 0)
}

fun findCharAfter(s: CharSequence, index: Int, c: Char): Int {
    if (s.isEmpty())
        return 0
    var pos = index
    while (pos < s.length && s[pos] != c)
        pos++
    return min(pos, s.length - 1)
}

fun getVisibleLines(view: TextView, scrollY: Int, height: Int):Pair<Int, Int> {
    return Pair(
            view.layout.getLineForVertical(scrollY),
            view.layout.getLineForVertical(scrollY + height))
}

fun isOpenParentheses(p: Char):Boolean {
    return "([{".indexOf(p) != -1
}

fun isClosedParentheses(p: Char):Boolean {
    return ")]}".indexOf(p) != -1
}
fun matchParentheses(open: Char, closed: Char):Boolean {
    return (open == '(' && closed == ')') || (open == '[' && closed == ']') || (open == '{' && closed == '}')
}