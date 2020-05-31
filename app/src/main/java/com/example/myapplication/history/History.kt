package com.example.myapplication.history

import android.text.Editable
import androidx.room.Entity
import androidx.room.Ignore

@Entity
class FileHistory() {
    interface ChangeOccured {
        fun onChange(undoAvailable: Boolean, redoAvailable: Boolean)
        fun onInsertHappen()
    }


    @Ignore
    var changeListeners: ArrayList<ChangeOccured> = ArrayList()
    var changes: ArrayList<TextChange> = ArrayList()
    var currentTop = 0


    fun addChangeOccuredListener(listener: ChangeOccured) {
        changeListeners.add(listener)
    }

    fun unregister(listener: ChangeOccured) {
        changeListeners.remove(listener)
    }

    fun addChange(change: TextChange) {
        if (currentTop == changes.size)
            changes.add(change)
        else {
            if (currentTop < changes.size) {
                changes.subList(currentTop, changes.size).clear()
                changes.add(change)
            } else
                currentTop = -1
        }
        currentTop++
        for (listener in changeListeners)
            listener.onChange(canUndo(), canRedo())

    }

    fun canUndo() = currentTop >=0 && currentTop <= changes.size && currentTop != 0

    fun canRedo() = currentTop >=0 && currentTop <= changes.size && currentTop != changes.size

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

class TextChange(var start: Int, var oldText: String, var newText: String)