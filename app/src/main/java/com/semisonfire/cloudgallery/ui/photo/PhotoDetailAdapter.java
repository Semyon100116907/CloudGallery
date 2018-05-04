package com.semisonfire.cloudgallery.ui.photo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

public class PhotoDetailAdapter extends PagerAdapter {

    private List<Photo> mPhotoList = new ArrayList<>();
    private Bitmap mCurrentItemBitmap;
    private int mOrientation;

    public Photo getItemByPosition(int position) {
        if (mPhotoList.size() > 0) {
            return mPhotoList.get(position);
        }

        return null;
    }

    public void setItems(List<Photo> mPhotos) {
        mPhotoList = mPhotos;
        notifyDataSetChanged();
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        //Bind view
        LayoutInflater layoutInflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_photo_detail, container, false);
        ImageView mPhoto = view.findViewById(R.id.image_photo_detailed);
        ProgressBar mProgressLoader = view.findViewById(R.id.progress_photo_loading);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mPhoto.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = Constraints.LayoutParams.WRAP_CONTENT;
            params.height = Constraints.LayoutParams.MATCH_PARENT;
        } else {
            params.width = Constraints.LayoutParams.MATCH_PARENT;
            params.height = Constraints.LayoutParams.WRAP_CONTENT;
        }
        mPhoto.setLayoutParams(params);

        WindowManager windowManager = (WindowManager)view.getContext().getSystemService(Context.WINDOW_SERVICE);
        int maxWidth = 0;
        int maxHeight = 0;
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            maxWidth = size.x;
            maxHeight = size.y;
        }
        mPhoto.setMaxWidth(maxWidth);
        mPhoto.setMaxHeight(maxHeight);

        Photo photo = mPhotoList.get(position);
        mProgressLoader.setVisibility(View.VISIBLE);

        final LoadTarget loadTarget = new LoadTarget(mPhoto, mProgressLoader);

        //Load image
        Picasso.get().load(photo.getPreview())
                .transform(new FullScreenTransform(maxWidth))
                .noFade()
                .placeholder(R.color.black)
                .into(loadTarget);
        mPhoto.setTag(loadTarget);

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mPhotoList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object){
        return PagerAdapter.POSITION_NONE;
    }

    public Bitmap getCurrentItemBitmap() {
        return mCurrentItemBitmap;
    }

    class LoadTarget implements Target {

        private ImageView imageView;
        private ProgressBar progressBar;

        public LoadTarget(ImageView imageView, ProgressBar progressBar) {
            this.imageView = imageView;
            this.progressBar = progressBar;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mCurrentItemBitmap = bitmap;
            imageView.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    class FullScreenTransform implements Transformation {

        private final int targetWidth;

        public FullScreenTransform(int targetWidth) {
            this.targetWidth = targetWidth;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            double aspectRatio = (double)source.getHeight() / (double)source.getWidth();
            int height = (int) (targetWidth * aspectRatio);
            Bitmap transformed = Bitmap.createScaledBitmap(source, targetWidth, height, false);

            if (transformed != source) {
                source.recycle();
                source = transformed;
            }
            return source;
        }

        @Override
        public String key() {
            return "transform_" + targetWidth;
        }
    }
}
