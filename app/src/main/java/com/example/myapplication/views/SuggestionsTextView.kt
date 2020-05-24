package com.example.myapplication.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import com.example.myapplication.R
import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter
import com.example.myapplication.SyntaxHighlight.Styler.Styler
import com.example.myapplication.SyntaxHighlight.Suggestions.suggestions
import com.example.myapplication.SyntaxHighlight.Tokens.BracketToken
import com.example.myapplication.settings.SettingsData
import com.example.myapplication.views.Tokenizer.CPlusPlusTokenizer

class SuggestionsTextView : AppCompatMultiAutoCompleteTextView {

    lateinit var highlighter: CPlusPlusHighlighter
    lateinit var scrollView: ScrollView
    lateinit var settingsData: SettingsData
    lateinit var styler: Styler

    private var updateFile = false

    var fileChanged: Boolean
        get() = updateFile
        set (value) {
            updateFile = value
            if (value) {
                if (this::highlighter.isInitialized) {
                    highlighter.parse(text)
                }
            }
        }
    var prevScrollY = -1f
    var toast: Toast? = null
    val suggestionList = suggestions.toCollection(ArrayList())
    var suggestionsSize = suggestionList.size
    lateinit var adapter: ArrayAdapter<String>

    constructor(context: Context):super(context)
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)


    fun initialize(highlighter: CPlusPlusHighlighter, scroll: ScrollView, settingsData: SettingsData, styler: Styler) {
        val list = ArrayList(suggestionList)
        adapter = ArrayAdapter<String>(context,
                R.layout.item_suggestion, list)
        setAdapter(adapter)
        setTokenizer(CPlusPlusTokenizer())
        this.highlighter = highlighter
        this.settingsData = settingsData
        this.styler = styler
        scrollView = scroll
        scrollView.addOnLayoutChangeListener(object : OnLayoutChangeListener{
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                dropDownHeight = (0.5 * scrollView.height).toInt()
                dropDownWidth = (0.5 * scrollView.width).toInt()
                changePopupPosition()
            }
        })
        scrollView.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                if (prevScrollY == scrollView.getScrollY().toFloat() || selectionStart != selectionEnd)
                    return
                val line: Int
                val startLine: Int = getLayout().getLineForVertical(scrollView.getScrollY() as Int)
                val endLine: Int = getLayout().getLineForVertical(scrollView.getScrollY() as Int + scrollView.getHeight())
                line = if (prevScrollY > scrollView.getScrollY()) {
                    startLine + 2
                } else endLine - 2
                if (line > 0 && line < getLineCount()) {
                    val charNumber: Int = getLayout().getLineStart(line)
                    setSelection(charNumber)
                }
                if (settingsData.codeHighlighting) {
                    styler.updateStyling(scrollView.getScrollY(), scrollView.getHeight())
                }
                prevScrollY = scrollView.getScrollY().toFloat()
            }
        })
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (fileChanged) {
                fileChanged = false
                styler.updateStyling(prevScrollY.toInt(), scroll.getHeight())
            }
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextBefore", "$start, $count")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("TextOn", "$start, $count")
                if (!fileChanged && settingsData.codeHighlighting) {
                    highlighter.update(s, start, start + count, count - before, selectionStart)
                    updateIdentifiersTokens(highlighter.identifiers())
                    styler.updateStyling(prevScrollY.toInt(), scroll.height)
                }
            }

            override fun afterTextChanged(s: Editable) {
//                if (recentlyEnteredString == "\n") {
//                    s.insert(codeEdit.getSelectionStart(), "\t\n")
//                    //                    s.insert(codeEdit.getSelectionStart(),"\n");
//                    codeEdit.setSelection(codeEdit.getSelectionStart() - 1)
//                }
            }
        })
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        changePopupPosition()
//        if (selStart != selEnd || !this::highlighter.isInitialized)
//            return
//        val brackets = highlighter.brackets()
//        var currentSelection: BracketToken? = null
//        for (b in brackets) {
//            if (b.start <= selStart) {
//                currentSelection = b
//
//            } else
//                break
//        }
//        var indent = 0
//        if (currentSelection != null)
//            indent = currentSelection.indentationLevel
//        if (toast != null)
//            toast!!.cancel()
//        toast = Toast.makeText(context, "Indentation level: ${indent}", Toast.LENGTH_LONG)
//        toast!!.show()
//        return
    }


    fun updateIdentifiersTokens(identifiers: HashSet<String>) {
//        suggestionList.subList(suggestionsSize, suggestionList.size).clear()
        adapter.clear()
        adapter.addAll(suggestionList + identifiers)
//        suggestionList.addAll(identifiers)
//        adapter.addAll(suggestionList+identifiers)
    }

    fun changePopupPosition() {
        if (layout == null || selectionStart != selectionEnd)
            return
        val cursorPos = selectionStart
        val cursorLine = layout.getLineForOffset(cursorPos)
        val textBottom = layout.getLineBottom(cursorLine)
        val scrollY = scrollView.scrollY
        var localTextPosition = -scrollY + textBottom + paddingTop
        if (scrollView.height - localTextPosition <= dropDownHeight ) {
            val textTop = layout.getLineTop(cursorLine)
            localTextPosition = -scrollY + textTop + paddingTop - dropDownHeight
        }
        dropDownVerticalOffset = dropDownHeight + localTextPosition
        dropDownHorizontalOffset = layout.getPrimaryHorizontal(cursorPos).toInt()
    }
}