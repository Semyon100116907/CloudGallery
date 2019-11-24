package com.semisonfire.cloudgallery.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.view.View
import android.view.ViewGroup
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.ui.dialogs.base.BaseDialogFragment

class BottomDialogFragment : BaseDialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = context
    val sheetView = View.inflate(context, R.layout.dialog_bottom, null)
    val behavior = BottomSheetBehavior.from(sheetView.parent as View)
    bind(sheetView)

    if (context == null) throw NullPointerException("Context must not be null")

    return BottomSheetDialog(context).apply {
      setContentView(sheetView)
      setOnShowListener {
        behavior.setPeekHeight(sheetView.height)
      }
    }
  }

  private fun bind(sheetView: View) {
    val clickListener = View.OnClickListener {
      dialogListener?.onItemClick(dialog, it)
    }

    val camera = sheetView.findViewById<ViewGroup>(R.id.container_camera)
    camera.setOnClickListener(clickListener)

    val gallery = sheetView.findViewById<ViewGroup>(R.id.container_gallery)
    gallery.setOnClickListener(clickListener)
  }
}