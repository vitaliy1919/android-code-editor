package com.example.myapplication.utils

import android.content.Context
import android.util.TypedValue


fun spToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}
fun dpToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, context.resources.displayMetrics)
}
