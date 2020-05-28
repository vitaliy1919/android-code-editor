package com.example.myapplication.history

import android.text.Editable

class FileHistory() {
    interface ChangeOccured {
        fun onChange(undoAvailable: Boolean, redoAvailable: Boolean)
        fun onInsertHappen()
    }



    private var changeListeners: ArrayList<ChangeOccured> = ArrayList()
    private var changes: ArrayList<TextChange> = ArrayList()
    private var currentTop = 0


    fun addChangeOccuredListener(listener: ChangeOccured) {
        changeListeners.add(listener)
    }

    fun addChange(change: TextChange) {
        if (currentTop == changes.size)
            changes.add(change)
        else {
            changes.subList(currentTop, changes.size).clear()
            changes.add(change)
        }
        currentTop++
        for (listener in changeListeners)
            listener.onChange(canUndo(), canRedo())

    }

    fun canUndo() = currentTop != 0

    fun canRedo() = currentTop != changes.size

    fun undo(s: Editable) {
        if (canUndo()) {
            val changeToUndo = changes[currentTop - 1]
            for (listener in changeListeners) {
                listener.onInsertHappen()
            }
            s.replace(changeToUndo.start, changeToUndo.start + changeToUndo.newText.length, changeToUndo.oldText)
            currentTop--
            for (listener in changeListeners) {
                listener.onChange(canUndo(), canRedo())
            }
        }
    }

    fun redo(s: Editable) {
        if (canRedo()) {
            val changeToUndo = changes[currentTop]
            for (listener in changeListeners) {
                listener.onInsertHappen()
            }
            s.replace(changeToUndo.start, changeToUndo.start + changeToUndo.oldText.length,  changeToUndo.newText)
            currentTop++
            for (listener in changeListeners) {
                listener.onChange(canUndo(), canRedo())
            }
        }
    }

}

class TextChange(var start: Int, var oldText: CharSequence, var newText: CharSequence)