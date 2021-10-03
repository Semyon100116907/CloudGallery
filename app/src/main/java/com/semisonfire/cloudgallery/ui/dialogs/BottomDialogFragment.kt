package com.semisonfire.cloudgallery.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.semisonfire.cloudgallery.R

class BottomDialogFragment : DialogFragment() {

    var dialogListener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.fragment_upload_chooser, null)
        bind(sheetView)

        bottomSheetDialog.setContentView(sheetView)

        val behavior = BottomSheetBehavior.from(sheetView.parent as View)
        return bottomSheetDialog.apply {
            setOnShowListener {
                behavior.setPeekHeight(sheetView.height)
            }
        }
    }

    private fun bind(sheetView: View) {
        val clickListener = View.OnClickListener {
            dialogListener?.onItemClick(it)
            dismiss()
        }

        val camera = sheetView.findViewById<TextView>(R.id.container_camera)
        camera.setOnClickListener(clickListener)

        val gallery = sheetView.findViewById<TextView>(R.id.container_gallery)
        gallery.setOnClickListener(clickListener)
    }
}