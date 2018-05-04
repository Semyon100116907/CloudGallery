package com.semisonfire.cloudgallery.ui.main.disk.adapter.items;

public abstract class DiskItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_GALLERY = 1;
    public static final int TYPE_UPLOAD = 2;

    /** Item type. */
    public abstract int getType();

    public abstract String getDate();
}