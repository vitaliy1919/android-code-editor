package com.example.myapplication.Files

import android.content.Context
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import java.io.*

class FileIO(var context: Context) {
    @Throws(IOException::class, FileNotFoundException::class)
    fun saveFile(fileUri: Uri, str: String) {
        val stream: OutputStream? = context.contentResolver.openOutputStream(fileUri)
        val writer = OutputStreamWriter(stream!!)
        writer.write(str)
        writer.close()
    }

    @Throws(IOException::class, FileNotFoundException::class)
    fun openFile(fileUri: Uri):String  {
        var startTime = System.currentTimeMillis()
        var inputStream: InputStream? = null
        val str = ""
        val buf = StringBuilder()
        inputStream = context.contentResolver.openInputStream(fileUri)
        val lines = 0
        val reader = BufferedInputStream(inputStream!!)
        val writer = ByteArrayOutputStream()
        val bytes = ByteArray(1024 * 1024)

        var chunkSize = 1
        while (chunkSize > 0) {
            chunkSize = reader.available()
            chunkSize = reader.read(bytes, 0, chunkSize)
            writer.write(bytes, 0, chunkSize)
        }
        var difference = System.currentTimeMillis() - startTime

        startTime = System.currentTimeMillis()
        val data1 = writer.toString()

        difference = System.currentTimeMillis() - startTime

        inputStream.close()
        reader.close()
        writer.close()
        return data1
    }
}