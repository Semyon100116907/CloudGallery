package com.semisonfire.cloudgallery.ui.main.dialogs.base;

import android.content.DialogInterface;
import android.view.View;

public interface DialogListener {

    void onPositiveClick(DialogInterface dialogInterface);

    void onNegativeClick(DialogInterface dialogInterface);

    void onItemClick(DialogInterface dialogInterface, View view);

}
