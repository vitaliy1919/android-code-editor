package com.example.myapplication.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R

class AutoCompleteTextViewWithNumbers : AppCompatMultiAutoCompleteTextView {

    val paint:Paint = Paint()
    var symbolWidth: Float = -1f
    var lineNumber: Int = -1
    var shouldUpdate: Boolean = true
    constructor(context: Context):super(context) {
        setText("LOL")

    }
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet) {
        setText("LOL")


    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        setText("LOL")

    }


    fun countDigits(i: Int):Int {
        if (i == 0)
            return 1
        var number = i
        var digits = 0
        while (number != 0) {
            digits++
            number /= 10
        }
        return digits
    }

    fun initPaints() {

        paint.setColor(ContextCompat.getColor(context, R.color.darkula_text))
        paint.textSize = spToPx(12f, context)
        paint.setTypeface(ResourcesCompat.getFont(context, R.font.jetbrains_mono))
        var width = -1.0f
        for (i in 0..9) {
            val currentMeasurement = paint.measureText(i.toString())
            if (currentMeasurement > width)
                width = currentMeasurement
        }
        symbolWidth = width
        setPadding(symbolWidth.toInt(), 0,0,0)
//        addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
//            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
//                if (lineNumber != lineCount && shouldUpdate) {
//                    shouldUpdate = false
//                    lineNumber = lineCount
//            }
//        })

//        addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                TODO("Not yet implemented")
//            }
//        })
    }
    fun spToPx(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
    }
    private fun dpToPixel(dp: Float): Float {
        val metrics: DisplayMetrics = context.resources.getDisplayMetrics()
        return dp * (metrics.densityDpi / 160f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (layout != null) {
            val maxDigits = countDigits(lineCount + 1)
            val maxWidth = symbolWidth*maxDigits
            setPadding(maxWidth.toInt(), 0, 0, 0)
            var curLine = 1
            for (i in 0..lineCount-1) {
                if (text[layout.getLineEnd(i) - 1] == '\n' || layout.getLineEnd(i) == text.length) {
                    val currentDigits = countDigits(curLine)
                    val curWidth = (maxDigits - currentDigits)*symbolWidth
                    canvas?.drawText(curLine.toString(), curWidth, layout.getLineBaseline(i).toFloat(), paint)
                    curLine++

                }
            }
//            canvas?.drawText("1", 0, 1, 0f, layout.getLineBaseline(0).toFloat(), paint)
        }
    }
}