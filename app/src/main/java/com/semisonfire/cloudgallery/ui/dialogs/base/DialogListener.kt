package com.semisonfire.cloudgallery.ui.dialogs.base

import android.content.DialogInterface
import android.view.View

interface DialogListener {

  fun onPositiveClick(dialogInterface: DialogInterface)

  fun onNegativeClick(dialogInterface: DialogInterface)

  fun onItemClick(dialogInterface: DialogInterface, view: View)
}

interface BottomDialogListener {

  fun onItemClick(dialogInterface: DialogInterface, view: View)
}
