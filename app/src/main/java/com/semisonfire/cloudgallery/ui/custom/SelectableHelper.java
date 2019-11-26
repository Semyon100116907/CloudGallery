package com.semisonfire.cloudgallery.ui.custom;

import com.semisonfire.cloudgallery.core.data.model.Photo;

import java.util.List;

public class SelectableHelper {

    private static boolean multipleSelection;

    public static void setMultipleSelection(boolean multipleSelection) {
        SelectableHelper.multipleSelection = multipleSelection;
    }

    public static boolean getMultipleSelection() {
        return SelectableHelper.multipleSelection;
    }

    public interface OnPhotoListener {

        void onPhotoClick(List<Photo> photos, int position);
        void onPhotoLongClick();
        void onSelectedPhotoClick(Photo photo);
    }
}
