package com.semisonfire.cloudgallery.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.semisonfire.cloudgallery.ui.dialogs.base.BaseDialogFragment;

public class AlertDialogFragment extends BaseDialogFragment {

    private static final String ARGUMENT_TITLE = "ARGUMENT_TITLE";
    private static final String ARGUMENT_MESSAGE = "ARGUMENT_MESSAGE";
    private static final String ARGUMENT_COLOR = "ARGUMENT_COLOR";

    private String mTitle;
    private String mMessage;
    private int mButtonColor = -1;

    public static AlertDialogFragment newInstance(String title, String message, int color) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_TITLE, title);
        args.putString(ARGUMENT_MESSAGE, message);
        args.putInt(ARGUMENT_COLOR, color);

        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.setArguments(args);
        return alertDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(ARGUMENT_TITLE);
            mMessage = args.getString(ARGUMENT_MESSAGE);
            mButtonColor = args.getInt(ARGUMENT_COLOR, -1);
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(mTitle != null ? mTitle : "")
                .setMessage(mMessage != null ? mMessage : "")
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    if (getDialogListener() != null) {
                        getDialogListener().onPositiveClick(dialogInterface);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                    if (getDialogListener() != null) {
                        getDialogListener().onNegativeClick(dialogInterface);
                    }
                })
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            if (mButtonColor != -1) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mButtonColor);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mButtonColor);
            }
        });
        return dialog;
    }
}
