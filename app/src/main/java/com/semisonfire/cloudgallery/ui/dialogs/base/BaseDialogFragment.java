package com.semisonfire.cloudgallery.ui.dialogs.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {

    private DialogListener mDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DialogListener) {
            mDialogListener = (DialogListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mDialogListener == null) {
            if (getTargetFragment() instanceof DialogListener) {
                mDialogListener = (DialogListener) getTargetFragment();
            }
        }
    }

    public DialogListener getDialogListener() {
        return mDialogListener;
    }

    public void setDialogListener(DialogListener listener) {
        mDialogListener = listener;
    }
}
