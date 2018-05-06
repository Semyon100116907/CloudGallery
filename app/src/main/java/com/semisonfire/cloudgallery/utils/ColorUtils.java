package com.semisonfire.cloudgallery.utils;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Menu;

public class ColorUtils {

    /** Change menu items icon color */
    public static void setMenuIconsColor(Menu menu, int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }
    }
}
