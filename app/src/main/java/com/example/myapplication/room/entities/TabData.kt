package com.example.myapplication.room.entities

import android.net.Uri
import androidx.room.*
import com.example.myapplication.history.FileHistory

@Entity
data class TabData(
        @PrimaryKey(autoGenerate = true) var id: Long,
        var fileName: String,
        var fileUri: Uri?,
        var initialText: String,
        var lineNumber: Int,
        @Embedded
        var fileHistory: FileHistory) {
    @Ignore
    constructor(fileName: String, fileUri: Uri? = null, initialText: String = ""):this(0, fileName, fileUri, initialText, 0, FileHistory())
}