package com.semisonfire.cloudgallery.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.semisonfire.cloudgallery.R

class AlertDialogFragment : DialogFragment() {

    var dialogListener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialogTitle = ""
        var dialogMessage = ""
        var dialogButtonColor = -1

        arguments?.apply {
            dialogTitle = getString(ARGUMENT_TITLE) ?: ""
            dialogMessage = getString(ARGUMENT_MESSAGE) ?: ""
            dialogButtonColor = getInt(ARGUMENT_COLOR, -1)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(R.string.action_ok) { dialogInterface, i ->
                dialogListener?.onPositiveClick()
                dialogInterface.cancel()
            }
            .setNegativeButton(R.string.action_cancel) { dialogInterface, i ->
                dialogListener?.onNegativeClick()
                dialogInterface.cancel()
            }
            .create()
        dialog.setOnShowListener {
            if (dialogButtonColor != -1) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
            }
        }
        return dialog
    }

    companion object {

        private const val ARGUMENT_TITLE = "ARGUMENT_TITLE"
        private const val ARGUMENT_MESSAGE = "ARGUMENT_MESSAGE"
        private const val ARGUMENT_COLOR = "ARGUMENT_COLOR"

        @JvmStatic
        fun newInstance(
            title: String?,
            message: String?,
            color: Int
        ): AlertDialogFragment {

            val args = Bundle()
            args.putString(ARGUMENT_TITLE, title)
            args.putString(ARGUMENT_MESSAGE, message)
            args.putInt(ARGUMENT_COLOR, color)

            val alertDialogFragment = AlertDialogFragment()
            alertDialogFragment.arguments = args
            return alertDialogFragment
        }
    }
}