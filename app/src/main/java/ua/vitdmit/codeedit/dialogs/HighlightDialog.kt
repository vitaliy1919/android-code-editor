package ua.vitdmit.codeedit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import ua.vitdmit.codeedit.R

class HighlightDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)
            // Set the dialog title
            builder.setTitle("Highlight as:")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setSingleChoiceItems(R.array.highlight_values, 1,DialogInterface.OnClickListener({dialog, position ->
                        Log.d("HighlightDialog", position.toString())
                    }) )
                    // Set the action buttons
                    .setPositiveButton("Ok",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK, so save the selectedItems results somewhere
                                // or return them to the component that opened the dialog
//                                ...
                            })
                    .setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialog, id ->
                            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}