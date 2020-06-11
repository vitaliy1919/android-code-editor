package ua.vitdmit.codeedit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class EncodingDialog(val codeEdit: TextView, var encoding:String) : DialogFragment() {
//    var encoding = ""
    init {

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)
            val charsets = Charset.availableCharsets()
            val charset_names = ArrayList<CharSequence>()
            val currentEncoding = encoding
            var selected = 0
            var i = 0
            for (value in charsets) {
                if (value.key == encoding)
                    selected = i
                charset_names.add(value.key)
                i++
            }
//            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, charset_names)
            // Set the dialog title
            builder.setTitle("Encodings:")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setSingleChoiceItems(charset_names.toTypedArray(), selected, DialogInterface.OnClickListener { dialog, position ->
                        Log.d("HighlightDialog", position.toString())
                        encoding = charset_names[position].toString()

                    })
                    // Set the action buttons
                    .setPositiveButton("Ok",
                            DialogInterface.OnClickListener { dialog, id ->
                                try {
                                    codeEdit.text = String(codeEdit.text.toString().toByteArray(charset(currentEncoding)), Charset.forName(encoding))
                                } catch (e: UnsupportedEncodingException) {
                                    Snackbar.make(codeEdit, "Charset is not supported", Snackbar.LENGTH_SHORT).show()
                                }
                            })
                    .setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialog, id ->
                            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}