package ua.vitdmit.codeedit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import ua.vitdmit.codeedit.R
import ua.vitdmit.codeedit.SyntaxHighlight.Languages

class HighlightDialog(var highLightPosition:Int) : DialogFragment() {
    interface Result {
        fun onHighlightResult(position: Int)
    }
    var position = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)
            // Set the dialog title
            builder.setTitle("Highlight as:")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setSingleChoiceItems(R.array.highlight_values, highLightPosition) { dialog, position ->
                        Log.d("HighlightDialog", position.toString())
                        this@HighlightDialog.position = position
                    }
                    // Set the action buttons
                    .setPositiveButton("Ok"
                    ) { dialog, id ->
                        if (activity is Result) {
                            (activity as Result).onHighlightResult(position)
                        }

                    }
                    .setNegativeButton("Cancel"
                    ) { dialog, id ->
                    }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}