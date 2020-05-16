package com.example.myapplication.views

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.R
import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter
import com.example.myapplication.SyntaxHighlight.Suggestions.suggestions
import com.example.myapplication.utils.getVisibleLines
import com.example.myapplication.views.Tokenizer.CPlusPlusTokenizer

class SuggestionsTextView : AppCompatMultiAutoCompleteTextView {

    private var lettersHeight: LinearLayout? = null
    val paint:Paint = Paint()
    var symbolWidth: Float = -1f
    var lineNumber: Int = -1
    var shouldUpdate: Boolean = true
    var highlighter: CPlusPlusHighlighter? = null
    var globalLayout: ConstraintLayout? = null
    var scrollView: ScrollView? = null


    constructor(context: Context):super(context) {
//        setText("LOL")

    }
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet) {
//        setText("LOL")


    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
//        setText("LOL")

    }

    fun initialize(highlighter: CPlusPlusHighlighter, scroll: ScrollView, globalLayout: ConstraintLayout, lettersHeight: LinearLayout) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context,
                R.layout.item_suggestion, suggestions)
        setAdapter(adapter)
        setTokenizer(CPlusPlusTokenizer())
        this.highlighter = highlighter
        this.globalLayout = globalLayout
        this.lettersHeight = lettersHeight
        scrollView = scroll
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d("Offset", dropDownVerticalOffset.toString())

                changePopupPosition()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        changePopupPosition()
        return super.onTouchEvent(event)

    }

//    override fun performClick(): Boolean {
//        changePopupPosition()
//
//        return super.performClick()
//    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
//        Log.d("Size", "$w $h")
        dropDownHeight = (0.5 * h).toInt()
        dropDownWidth = (0.5 * w).toInt()
        changePopupPosition()
    }

    private fun getVisibleHeight(): Int {
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        return rect.bottom - rect.top
    }
    fun changePopupPosition() {
        if (selectionStart != selectionEnd || globalLayout == null || lettersHeight == null)
            return
//        val rect = Rect()
//        getWindowVisibleDisplayFrame(rect)
        val cursorPos = selectionStart
        val height = globalLayout!!.height
        val cursorLine = layout.getLineForOffset(cursorPos)
        val textBottom = layout.getLineBottom(cursorLine)
        val scrollY = scrollView!!.scrollY
//        dropDownVerticalOffset = 0
//        val charHeight = paint.measureText("M").toInt()
//        val line = layout.getLineForOffset(selectionStart)
//        val baseline = layout.getLineBaseline(line)
//        val ascent = layout.getLineAscent(line)
//
//        val x = layout.getPrimaryHorizontal(selectionStart)
//        val y = baseline + ascent
//
//
//        val offsetVertical = y + charHeight - scrollY
//
//        var tmp = offsetVertical + dropDownHeight + charHeight
//        if (tmp < getVisibleHeight()) {
//            tmp = offsetVertical + charHeight / 2
//            dropDownVerticalOffset = tmp
//        } else {
//            tmp = offsetVertical - dropDownHeight - charHeight
//            dropDownVerticalOffset = tmp
//        }
        var localTextPosition = -scrollY + textBottom + paddingTop
        if (scrollView!!.height - localTextPosition <= dropDownHeight ) {
            val textTop = layout.getLineTop(cursorLine)
            localTextPosition = -scrollY + textTop + paddingTop - dropDownHeight
        }
        dropDownVerticalOffset = dropDownHeight + localTextPosition
        Log.d("Offset", dropDownVerticalOffset.toString())

        val (firstVisibleLine, lastVisibleLine) = getVisibleLines(this, globalLayout!!.scrollY, globalLayout!!.height)

    }
}