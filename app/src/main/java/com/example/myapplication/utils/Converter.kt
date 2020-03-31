package com.example.myapplication.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.max
import kotlin.math.min


fun spToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}
fun dpToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, context.resources.displayMetrics)
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
