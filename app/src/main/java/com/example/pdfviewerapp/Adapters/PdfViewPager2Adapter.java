package com.example.pdfviewerapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pdfviewerapp.R;

import java.util.ArrayList;
import java.util.List;

public class PdfViewPager2Adapter extends RecyclerView.Adapter<PdfViewPager2Adapter.ViewHolder> {

    private ArrayList<Bitmap> bitmapsList;
    private LayoutInflater mInflater;
    private ViewPager2 viewPager2;
    public PdfViewPager2Adapter(Context context, ArrayList<Bitmap> bitmapsList, ViewPager2 viewPager2) {
        this.mInflater = LayoutInflater.from(context);
        this.bitmapsList = bitmapsList;
        this.viewPager2 = viewPager2;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_view_view_pager2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.imageView.setImageBitmap(bitmapsList.get(position));
    }

    @Override
    public int getItemCount() {
        return bitmapsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_view_viewpager_iv);
            itemView.setScrollbarFadingEnabled(true);

        }

    }
}
