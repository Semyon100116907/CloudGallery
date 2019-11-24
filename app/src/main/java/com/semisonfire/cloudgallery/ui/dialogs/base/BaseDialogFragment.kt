package com.semisonfire.cloudgallery.ui.dialogs.base

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment

open class BaseDialogFragment : DialogFragment() {

  protected var dialogListener: DialogListener? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is DialogListener) {
      dialogListener = context
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (dialogListener != null) return
    if (targetFragment is DialogListener) {
      dialogListener = targetFragment as DialogListener?
    }
  }

}