package com.semisonfire.cloudgallery.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.ui.dialogs.base.BaseDialogFragment;

public class BottomDialogFragment extends BaseDialogFragment {

    private BottomSheetDialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialog = new BottomSheetDialog(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View sheetView = View.inflate(getContext(), R.layout.dialog_bottom, null);
        bind(sheetView);

        dialog.setContentView(sheetView);

        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) sheetView.getParent());
        dialog.setOnShowListener(dialogInterface -> behavior.setPeekHeight(sheetView.getHeight()));

        return dialog;
    }

    private void bind(View sheetView) {
        View.OnClickListener clickListener = view -> {
            if (getDialogListener() != null) getDialogListener().onItemClick(this.getDialog(), view);
        };

        ViewGroup mCamera = sheetView.findViewById(R.id.container_camera);
        mCamera.setOnClickListener(clickListener);

        ViewGroup mGallery = sheetView.findViewById(R.id.container_gallery);
        mGallery.setOnClickListener(clickListener);
    }
}
