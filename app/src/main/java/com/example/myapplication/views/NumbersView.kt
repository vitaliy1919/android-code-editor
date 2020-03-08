package com.example.myapplication.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.MultiAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R

class NumbersView : View {
    constructor(context: Context):super(context)
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    private var viewWidth = 10
    private var viewHeight = 10
    private var symbolWidth: Float = -1F
    private var textPaint = Paint()
    var codeEdit: MultiAutoCompleteTextView? = null

    fun initializeView(edit: MultiAutoCompleteTextView) {
        codeEdit = edit
        initPaints()
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
        textPaint.setColor(ContextCompat.getColor(context, R.color.darkula_text))
        textPaint.textSize = spToPx(12f, context)
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.jetbrains_mono))
        var width = -1.0f
        for (i in 0..9) {
            val currentMeasurement = textPaint.measureText(i.toString())
            if (currentMeasurement > width)
                width = currentMeasurement
        }
        symbolWidth = width
        setPadding(symbolWidth.toInt(), 0,0,0)
    }
    fun spToPx(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(viewWidth, viewHeight)
    }

    fun update() {
        requestLayout();
        invalidate();
    }
    override fun onDraw(canvas: Canvas?) {
        Log.d("NumbersView", "Repainted!")
        super.onDraw(canvas)
        if (codeEdit?.layout != null) {
            val layout = codeEdit!!.layout
            val lineCount = codeEdit!!.lineCount
            val text = codeEdit!!.text
            val maxDigits = countDigits(lineCount + 1)
            val maxWidth = symbolWidth*maxDigits
            viewWidth = maxWidth.toInt()+1
            setPadding(maxWidth.toInt(), 0, 0, 0)
            var curLine = 1
            for (i in 0..lineCount-1) {
                if (layout.getLineEnd(i) != 0 &&
                        text[layout.getLineEnd(i) - 1] == '\n' || layout.getLineEnd(i) == text.length) {
                    val currentDigits = countDigits(curLine)
                    val curWidth = (maxDigits - currentDigits)*symbolWidth
                    canvas?.drawText(curLine.toString(), curWidth, layout.getLineBaseline(i).toFloat(), textPaint)
                    curLine++

                }
            }
            viewHeight = layout.getLineBaseline(lineCount - 1)
//            canvas?.drawText("1", 0, 1, 0f, layout.getLineBaseline(0).toFloat(), paint)
        }
    }
}