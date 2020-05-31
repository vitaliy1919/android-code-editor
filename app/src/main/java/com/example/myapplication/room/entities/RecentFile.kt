package com.example.myapplication.room.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentFile(
        @PrimaryKey(autoGenerate = true)
        var id:Long,
        var fileName: String,
        var fileUri: Uri)