package com.semisonfire.cloudgallery.ui.dialogs

import android.view.View

abstract class DialogListener {

    open fun onPositiveClick() {}
    open fun onNegativeClick() {}
    open fun onItemClick(view: View) {}
}
