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
import com.example.myapplication.R
import com.example.myapplication.utils.dpToPx
import kotlin.math.max
import kotlin.math.min

class FastScroll: View {
    private var stopScroll: Boolean = false
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
        val topOffset = percent*(measuredHeight - activeHeight)
        activeRect.offsetTo(0f, topOffset)
        unactiveRect.offsetTo(unactiveWidthPadding, topOffset + unactiveHeightPadding)
//        val currentPosition = (unactiveRect.top + unactiveRect.bottom) / 2.0f
//        var offset = percent*measuredHeight - currentPosition
//        unactiveRect.offset(0f, offset)
//        activeRect.offset(0f, offset)
//        if (activeRect.top < 0) {
//            activeRect.offsetTo(0f, 0f)
//            unactiveRect.offsetTo(unactiveWidthPadding, unactiveHeightPadding)
//        } else if (activeRect.bottom > measuredHeight) {
//            activeRect.offsetTo(0f, measuredHeight.toFloat() - activeHeight)
//            unactiveRect.offsetTo(unactiveWidthPadding, measuredHeight - unactiveHeightPadding  - unactiveHeight)
//        }
//        invalidate()
    }
    fun initialize(edit: MultiAutoCompleteTextView, scroll: ScrollView) {

        codeEdit = edit
        editScroll = scroll
        codeEdit?.addOnLayoutChangeListener(object:View.OnLayoutChangeListener{
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {

                val state = codeEdit!!.height <= editScroll!!.height
                if (state != isHidden) {
                    isHidden = state
                    invalidate()
                }
            }
        })
        editScroll?.viewTreeObserver?.addOnScrollChangedListener {
            if (stopScroll) {
                stopScroll = false
                editScroll?.scrollBy(0,0)
            }
            val curScroll =  editScroll!!.scrollY
            val maxScroll = editScroll!!.getChildAt(0).height - editScroll!!.height
            val percent = if (maxScroll == 0) 0f else curScroll / maxScroll.toFloat()
            setPercentage(percent)
            Log.d("Percent", "$percent, ${curScroll}, ${maxScroll}, ${maxScroll - curScroll}".toString())
            invalidate()
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
        val y = min(max(event.y, 0f),measuredHeight - activeHeight / 2)
        val percent = y / (measuredHeight - activeHeight / 2)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!activeRect.contains(event.x, event.y)) {
                    isActive = false
                    return false;
                }
                stopScroll = true
                isActive = true
            }
            MotionEvent.ACTION_MOVE -> {
                stopScroll = false
                setPercentage(percent)
            }
            MotionEvent.ACTION_UP -> {
                isActive = false
            }
        }
        invalidate()
//        Log.d("EventY", event.y.toString())
//        val progress = y / measuredHeight.toDouble()
        val lineNumber = ((codeEdit!!.lineCount -1) * percent).toInt()
        editScroll?.post(Runnable {
            val y: Int = codeEdit!!.getLayout().getLineTop(lineNumber) // e.g. I want to scroll to line 40
            editScroll?.scrollTo(0, y)
        })
        return true

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        canvas?.drawRoundRect(RectF(0f,0f,10000f,10000f), 5f, 5f, paint)
        canvas?.drawRoundRect(unactiveRect, 5f, 5f, paint)

        if (canvas != null && !isHidden) {
            if (isActive)
                canvas.drawRoundRect(activeRect, 15f, 15f, paintActive)
            canvas.drawRoundRect(unactiveRect, 5f, 5f, paint)
        }
//        canvas?.drawRoundRect(RectF(0f,0f,dpToPx(10f, context),measuredHeight.toFloat()),5f,5f,paint)
    }
}