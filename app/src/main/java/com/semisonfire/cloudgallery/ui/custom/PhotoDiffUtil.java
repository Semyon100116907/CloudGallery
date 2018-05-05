package com.semisonfire.cloudgallery.ui.custom;

import android.support.v7.util.DiffUtil;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

public class PhotoDiffUtil extends DiffUtil.Callback {

    List<Photo> newList;
    List<Photo> oldList;

    public PhotoDiffUtil(List<Photo> newList, List<Photo> oldList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Photo newItem = newList.get(newItemPosition);
        Photo oldItem = oldList.get(oldItemPosition);
        return oldItem.compareTo(newItem) == 0;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Photo newItem = newList.get(newItemPosition);
        Photo oldItem = oldList.get(oldItemPosition);
        return oldItem.equals(newItem);
    }
}
