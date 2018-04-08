package com.semisonfire.cloudgallery.utils;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.TypedValue;
import android.widget.TextView;

import java.lang.reflect.Field;

public class BottomBarUtils {
    @SuppressLint("RestrictedApi")
    public static void removeShiftingMode(BottomNavigationView bottomNavigationView, boolean enableAnim) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);

        setField(menuView.getClass(), menuView, "mShiftingMode", false);

        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
            item.setShiftingMode(false);
            item.setChecked(item.getItemData().isChecked());


            if (!enableAnim) {
                TextView mLargeLabel = getField(item.getClass(), item, "mLargeLabel");
                TextView mSmallLabel = getField(item.getClass(), item, "mSmallLabel");
                assert mLargeLabel != null;
                assert mSmallLabel != null;
                mLargeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallLabel.getTextSize());

                setField(item.getClass(), item, "mShiftAmount", 0);
                setField(item.getClass(), item, "mScaleUpFactor", 1);
                setField(item.getClass(), item, "mScaleDownFactor", 1);
            }
        }
        menuView.updateMenuView();
    }

    private static void setField(Class targetClass, Object instance, String fieldName, Object value) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private static <T> T getField(Class targetClass, Object instance, String fieldName) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
