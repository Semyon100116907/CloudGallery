package com.semisonfire.cloudgallery.ui.main.disk.adapter.items;

public class HeaderItem extends DiskItem {

    private String date;
    private int count;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HeaderItem) {
            return date.equals(((HeaderItem)obj).getDate());
        }

        return super.equals(obj);
    }
}
