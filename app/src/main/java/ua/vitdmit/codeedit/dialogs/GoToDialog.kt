package ua.vitdmit.codeedit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import ua.vitdmit.codeedit.R
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class GoToDialog(val scrollView: ScrollView, val lineNumber: Int) : DialogFragment() {
    //    var encoding = ""
    init {

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            val root = inflater.inflate(R.layout.dialog_goto, null)
            val editText = root.findViewById<TextInputLayout>(R.id.line_number)
            val seekBar = root.findViewById<SeekBar>(R.id.line_scroll)
            seekBar.max = lineNumber - 1
            seekBar.progress = 0
            var changeText = false
            var changeProgress = false
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    changeProgress = true
                    if (!changeText) {
                        editText.editText!!.setText((progress + 1).toString())
                    }
                    changeProgress = false
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            editText.addOnEditTextAttachedListener {
                editText.editText!!.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (s == null || s.toString().isEmpty() || changeProgress)
                            return
                        changeText = true
                        val a = s.toString().toInt()
                        if ( a < lineNumber) {
                            seekBar.progress = a - 1
                        }
                        changeText = false

                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                    TODO("Not yet implemented")
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    TODO

                    }
                })
            }

//            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, charset_names)
            // Set the dialog title
            builder.setTitle("Go to line (1..${lineNumber}):")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setView(root)
                    // Set the action buttons
                    .setPositiveButton("Ok",
                            DialogInterface.OnClickListener { dialog, id ->
                                if (!editText.editText!!.text.toString().isEmpty()) {
                                    val value = editText.editText!!.text.toString().toInt()
                                    if (value <= lineNumber) {
                                        val percent = value / lineNumber.toDouble()
                                        val y: Int = ((scrollView.getChildAt(0).height)*percent).toInt()
                                        scrollView.smoothScrollTo(0, y)
                                    }

                                }

                            })
                    .setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialog, id ->
                            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}