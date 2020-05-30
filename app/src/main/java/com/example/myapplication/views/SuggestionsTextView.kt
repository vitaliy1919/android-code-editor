package com.example.myapplication.views

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.core.content.ContextCompat
import com.example.myapplication.AppExecutors
import com.example.myapplication.R
import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter
import com.example.myapplication.SyntaxHighlight.Styler.Styler
import com.example.myapplication.SyntaxHighlight.Suggestions.suggestions
import com.example.myapplication.SyntaxHighlight.Tokens.BracketToken
import com.example.myapplication.history.FileHistory
import com.example.myapplication.history.TextChange
import com.example.myapplication.settings.SettingsData
import com.example.myapplication.views.Tokenizer.CPlusPlusTokenizer
import java.io.File
import kotlin.math.max

class SuggestionsTextView : AppCompatMultiAutoCompleteTextView {

    private lateinit var highlighter: CPlusPlusHighlighter
    private lateinit var scrollView: ScrollView
    private lateinit var settingsData: SettingsData
    private lateinit var styler: Styler
    private var firstBracketSpan: CharacterStyle? = null
    private var lastBracketSpan: CharacterStyle? = null

    var delayStylerUpdate = false
    private var somethingEntered = false
    private var lastChange = ""
    private var beforeLastChange = ""

    var startChange = -1
    var endChange = -1
    var fileHistoryUsed = false
    var fileChanged = false
    var prevScrollY = -1f
    var toast: Toast? = null
    val suggestionList = suggestions.toCollection(ArrayList())
    var suggestionsSize = suggestionList.size
    lateinit var fileHistory: FileHistory
    lateinit var adapter: ArrayAdapter<String>

    constructor(context: Context):super(context)
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)


    fun initialize(highlighter: CPlusPlusHighlighter, scroll: ScrollView, settingsData: SettingsData, styler: Styler, fileHistory: FileHistory) {
        this.fileHistory = fileHistory
        this.settingsData = settingsData
        changeCodeCompletion(settingsData.codeCompletion)
        setTokenizer(CPlusPlusTokenizer())
        this.highlighter = highlighter
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
        fileHistory.addChangeOccuredListener(object : FileHistory.ChangeOccured{
            override fun onInsertHappen() {
                fileHistoryUsed = true
            }

            override fun onChange(undoAvailable: Boolean, redoAvailable: Boolean) {
//                fileHistoryUsed = true
            }
        })
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (fileChanged || delayStylerUpdate) {
                fileChanged = false
                delayStylerUpdate = false
                if (settingsData.codeHighlighting) {
                    styler.updateStyling(prevScrollY.toInt(), scroll.getHeight())
                }
            }
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextBefore", "$start, $count")
                if (s == null)
                    beforeLastChange = ""
                else
                    beforeLastChange = s.subSequence(start, start + count).toString()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                startChange = start
                endChange = start + count
                lastChange = s.subSequence(start, start + count).toString()
                Log.d("TextOn", "$start, $count")
                if (!fileChanged && settingsData.codeHighlighting) {
                    highlighter.update(s, start, start + count, count - before, selectionStart)
                    updateIdentifiersTokens(highlighter.identifiers())
                    if (layout != null) {
                        styler.updateStyling(prevScrollY.toInt(), scroll.height)
                    } else
                        delayStylerUpdate = true
                }
            }

            override fun afterTextChanged(s: Editable) {
                somethingEntered = true
                if (!fileHistoryUsed) {
                    fileHistory.addChange(TextChange(startChange, beforeLastChange, lastChange))
                }
                fileHistoryUsed = false
                if (settingsData.autoIndent ) {
                    val indent = detectCurrentIndentation()
                    if (lastChange == "\n") {
                        if (endChange < s.length && s[endChange] == '}') {
                            text.insert(selectionStart, "\t".repeat(indent) + "\n")
                            setSelection(selectionStart - 1)
                        } else
                            text.insert(selectionStart, "\t".repeat(indent))
                    } else if (lastChange == "}") {
                        var i = startChange - 1
                        while (i >= 0 && s[i] != '\n' && s[i].isWhitespace()) {
                            i--
                        }
                        if (i+1 < startChange)
                            text.replace(i+1, startChange, "\t".repeat(max(indent - 1, 0)))
                    }
                }
            }
        })
    }

    private fun getCurrentBracket(): BracketToken? {
        if (selectionStart != selectionEnd || !this::highlighter.isInitialized)
            return null
        val brackets = highlighter.brackets()
        var currentSelection: BracketToken? = null
        for (b in brackets) {
            if (b.end <= selectionStart) {
                currentSelection = b
            } else
                break
        }
        return currentSelection
    }

    private fun detectCurrentIndentation(): Int {
        val currentSelection = getCurrentBracket()
        var indent = 0
        if (currentSelection != null) {
            indent = currentSelection.indentationLevel
        }
        return indent
    }
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (this::highlighter.isInitialized) {
            if (isPopupShowing && !somethingEntered)
                dismissDropDown()
            else
                changePopupPosition()
        }
        if (!isEnabled || (this::settingsData.isInitialized && !settingsData.codeHighlighting))
            return
        somethingEntered = false
        if (firstBracketSpan != null) {
            text.removeSpan(firstBracketSpan)
            firstBracketSpan = null
        }
        if (lastBracketSpan != null) {
            text.removeSpan(lastBracketSpan)
            lastBracketSpan = null
        }
        if (selectionStart != selectionEnd || !this::highlighter.isInitialized)
            return
        val brackets = highlighter.brackets()
        var currentSelection: BracketToken? = null
        for (b in brackets) {
            if (b.start == selectionStart || b.end == selectionStart) {
                currentSelection = b
                break
            }
        }
        if (currentSelection != null) {
            firstBracketSpan = BackgroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary))
            text.setSpan(firstBracketSpan,currentSelection.start, currentSelection.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (currentSelection.matchingBracket != null) {
                val matchingBracket = currentSelection.matchingBracket!!
                lastBracketSpan = BackgroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary))
                text.setSpan(lastBracketSpan,matchingBracket.start, matchingBracket.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }


    fun updateIdentifiersTokens(identifiers: HashSet<String>) {
        if (this::settingsData.isInitialized && settingsData.codeCompletion) {
            adapter.clear()
            adapter.addAll(suggestionList + identifiers)
        }
    }

    fun updateText(s: CharSequence) {
        isEnabled = false
        fileChanged = true
        setText(s)
        if (this::settingsData.isInitialized && settingsData.codeHighlighting)
            AppExecutors.diskIO.execute {
                highlighter.parse(s)
                Handler(Looper.getMainLooper()).post{
                    isEnabled = true
                    if (!fileChanged)
                        styler.updateStyling(scrollView.scrollY, scrollView.height)
                }
            }
    }

    fun changeSyntaxHighlight(value: Boolean) {
        if (this::highlighter.isInitialized) {
            if (!value) {
                highlighter.parse(text)
            } else {
                val spansToRemove: Array<Any> = text.getSpans(0, layout.getLineEnd(getLineCount() - 1), Any::class.java)
                for (span in spansToRemove) {
                    if (span is CharacterStyle) text.removeSpan(span)
                }
            }
        }
    }
    fun changeCodeCompletion(value: Boolean) {
        if (value) {
            val list = ArrayList(suggestionList)
            adapter = ArrayAdapter<String>(context,R .layout.item_suggestion, list)
            setAdapter(adapter)
        } else {
            setAdapter(null)
        }
    }
    fun changePopupPosition() {
        if (layout == null || selectionStart != selectionEnd ||
                (this::settingsData.isInitialized && !settingsData.codeCompletion))
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