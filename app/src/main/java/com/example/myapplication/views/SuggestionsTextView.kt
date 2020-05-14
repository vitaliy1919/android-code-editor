package com.example.myapplication.views

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import com.example.myapplication.R
import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter
import com.example.myapplication.SyntaxHighlight.Highlighter
import com.example.myapplication.SyntaxHighlight.Suggestions.suggestions
import com.example.myapplication.views.Tokenizer.CPlusPlusTokenizer

class SuggestionsTextView : AppCompatMultiAutoCompleteTextView {

    val paint:Paint = Paint()
    var symbolWidth: Float = -1f
    var lineNumber: Int = -1
    var shouldUpdate: Boolean = true
    var highlighter: CPlusPlusHighlighter? = null
    constructor(context: Context):super(context) {
//        setText("LOL")

    }
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet) {
//        setText("LOL")


    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
//        setText("LOL")

    }

    fun initialize(highlighter: CPlusPlusHighlighter) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context,
                R.layout.item_suggestion, suggestions)
        setAdapter(adapter)
        setTokenizer(CPlusPlusTokenizer())
        this.highlighter = highlighter

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
//        Log.d("Size", "$w $h")
        dropDownHeight = (0.5 * h).toInt()
        dropDownWidth = (0.5 * w).toInt()
    }

    fun changePopupPosition() {
//        sele
    }
}