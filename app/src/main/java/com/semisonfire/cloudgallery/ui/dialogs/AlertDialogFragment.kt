package com.semisonfire.cloudgallery.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.ui.dialogs.base.BaseDialogFragment

class AlertDialogFragment : BaseDialogFragment() {

  private var dialogTitle: String? = null
  private var dialogMessage: String? = null
  private var dialogButtonColor = -1

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    arguments?.apply {
      dialogTitle = getString(ARGUMENT_TITLE)
      dialogMessage = getString(ARGUMENT_MESSAGE)
      dialogButtonColor = getInt(ARGUMENT_COLOR, -1)
    }

    val dialog = AlertDialog.Builder(context)
      .setTitle(dialogTitle ?: "")
      .setMessage(dialogMessage ?: "")
      .setPositiveButton(R.string.action_ok) { dialogInterface, i ->
        dialogListener?.onPositiveClick(dialogInterface)
      }
      .setNegativeButton(R.string.action_cancel) { dialogInterface, i ->
        dialogListener?.onNegativeClick(dialogInterface)
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