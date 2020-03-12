package com.example.myapplication.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.MultiAutoCompleteTextView
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.graphics.plus
import com.example.myapplication.R
import com.example.myapplication.utils.dpToPx
import kotlin.math.max
import kotlin.math.min

class FastScroll: View {
    val activeHeight = dpToPx(45f, context)
    val unactiveHeight = dpToPx(34f, context)
    val unactiveHeightPadding = (activeHeight - unactiveHeight) / 2
    val activeWidth = dpToPx(18f, context)
    val unactiveWidth = dpToPx(8f, context)
    val rightPadding = dpToPx(1f, context)
    val unactiveWidthPadding = (activeWidth - unactiveWidth) / 2

    var isActive = false
    var isHidden = true
    val paint = Paint()
    val paintActive = Paint()
    val unactiveRect = RectF()
    val activeRect = RectF()
    var codeEdit: MultiAutoCompleteTextView? = null
    var editScroll: ScrollView? = null
    constructor(context: Context):super(context)
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet) {
//        paint.setColor(Color.RED)


    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(activeWidth.toInt(), measuredHeight)
    }

    fun setPercentage(percent: Float) {
        val currentPosition = (unactiveRect.top + unactiveRect.bottom) / 2.0f
        var offset = percent*measuredHeight - currentPosition
        unactiveRect.offset(0f, offset)
        activeRect.offset(0f, offset)
        if (activeRect.top < 0) {
            activeRect.offsetTo(0f, 0f)
            unactiveRect.offsetTo(unactiveWidthPadding, unactiveHeightPadding)
        } else if (activeRect.bottom > measuredHeight) {
            activeRect.offsetTo(0f, measuredHeight.toFloat() - activeHeight)
            unactiveRect.offsetTo(unactiveWidthPadding, measuredHeight - unactiveHeightPadding  - unactiveHeight)
        }
        invalidate()
    }
    fun initialize(edit: MultiAutoCompleteTextView, scroll: ScrollView) {

        codeEdit = edit
        editScroll = scroll
        codeEdit?.addOnLayoutChangeListener(object:View.OnLayoutChangeListener{
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {

                val state = codeEdit!!.height <= editScroll!!.height
                if (state != isHidden) {
                    invalidate()
                    isHidden = state
                }
            }
        })
        editScroll?.viewTreeObserver?.addOnScrollChangedListener {
            val percent = editScroll!!.scrollY / editScroll!!.getChildAt(0).height.toFloat()
            setPercentage(percent)
        }
        paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        paintActive.color = ContextCompat.getColor(context, R.color.colorPrimary)
        paintActive.alpha = 50


        activeRect.set(0f,0f,activeWidth,activeHeight)
        unactiveRect.set(
                unactiveWidthPadding,
                unactiveHeightPadding,
                unactiveWidthPadding + unactiveWidth,
                unactiveHeightPadding + unactiveHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return true
        val y = min(max(event.y, 0f),measuredHeight.toFloat())
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!activeRect.contains(event.x, event.y)) {
                    isActive = false
                    return false;
                }
                isActive = true
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPosition = (unactiveRect.top + unactiveRect.bottom) / 2.0f
                var offset = y.toFloat() - currentPosition
                unactiveRect.offset(0f, offset)
                activeRect.offset(0f, offset)
                if (activeRect.top < 0) {
                    activeRect.offsetTo(0f, 0f)
                    unactiveRect.offsetTo(unactiveWidthPadding, unactiveHeightPadding)
                } else if (activeRect.bottom > measuredHeight) {
                    activeRect.offsetTo(0f, measuredHeight.toFloat() - activeHeight)
                    unactiveRect.offsetTo(unactiveWidthPadding, measuredHeight - unactiveHeightPadding  - unactiveHeight)
                }
            }
            MotionEvent.ACTION_UP -> {
                isActive = false
            }
        }
        invalidate()
//        Log.d("EventY", event.y.toString())
        val progress = y / measuredHeight.toDouble()
        val lineNumber = ((codeEdit!!.lineCount -1) * progress).toInt()
        editScroll?.post(Runnable {
            val y: Int = codeEdit!!.getLayout().getLineTop(lineNumber) // e.g. I want to scroll to line 40
            editScroll?.scrollTo(0, y)
        })
        return true

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null && !isHidden) {
            if (isActive)
                canvas.drawRoundRect(activeRect, 15f, 15f, paintActive)
            canvas.drawRoundRect(unactiveRect, 5f, 5f, paint)
        }
//        canvas?.drawRoundRect(RectF(0f,0f,dpToPx(10f, context),measuredHeight.toFloat()),5f,5f,paint)
    }
}