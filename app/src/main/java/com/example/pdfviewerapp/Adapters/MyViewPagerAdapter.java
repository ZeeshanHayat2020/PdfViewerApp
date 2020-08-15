package com.example.pdfviewerapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class MyViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<Bitmap> bitmapsList;
    private OnImageClickLitener mListener;

    public MyViewPagerAdapter(Context context, ArrayList<Bitmap> bitmapsList) {
        mContext = context;
        this.bitmapsList = bitmapsList;

    }

    public interface OnImageClickLitener {
        void onClick();
    }

    public void setOnClickListener(OnImageClickLitener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return bitmapsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ImageView imageView = new ImageView(mContext);
        imageView.setImageBitmap(bitmapsList.get(position));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onClick();
                }
            }
        });
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}

