package com.semisonfire.cloudgallery.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentUtils {

    public static void changeFragment(FragmentManager manager, Fragment fragment, int containerViewId) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(containerViewId, fragment);
        transaction.commit();
    }
}
