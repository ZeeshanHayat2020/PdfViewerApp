package com.example.pdfviewerapp.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.pdfviewerapp.Adapters.MyViewPagerAdapter;
import com.example.pdfviewerapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PdfViewActivity extends AppCompatActivity {

    String TAG = "ActivityPdfRenderView";
    private PdfRenderer renderer;
    private PdfRenderer.Page currentPage;
    private ProgressBar loadingBar;
    private Button prevBtn;
    private Button nextBtn;
    private ParcelFileDescriptor parcelFileDescriptor;
    private ViewPager viewPager;
    private MyViewPagerAdapter viewPagerAdapter;
    private ArrayList<Bitmap> pdfImagesList;
    private int currentPageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        Intent intent = getIntent();

        loadingBar=(ProgressBar) findViewById(R.id.loadingBar);
        prevBtn = (Button) findViewById(R.id.btnPrev);
        nextBtn = (Button) findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(onClickListener);
        prevBtn.setOnClickListener(onClickListener);

    }


    @Override
    protected void onStart() {
        super.onStart();
        new LoadFiles().execute();
    }
    private void initRenderer() {
        try {
            AssetManager assetManager = getAssets();
            InputStream in = null;
            OutputStream out = null;
            File tempFile = new File(getFilesDir(), "temp.pdf");
            try {
                in = assetManager.open("mydoc.pdf");
                out = openFileOutput(tempFile.getName(), Context.MODE_PRIVATE);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
            parcelFileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(parcelFileDescriptor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "initRenderer: Error File Not Found");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void initViewPager(){
        viewPager=findViewById(R.id.viewPager);
        viewPagerAdapter = new MyViewPagerAdapter(this, pdfImagesList);
        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPageIndex=position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    private void getBitmapsFromRenderer(){
        pdfImagesList= new ArrayList<>();
        for (int i = 0; i < renderer.getPageCount(); i++) {
            if (currentPage != null) {
                currentPage.close();
            }
            currentPage = renderer.openPage(i);
            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfImagesList.add(bitmap);
            Log.d(TAG, "renderPage: Page Num" + i);
        }
    }
    private void updateViewPager(int currentPageIndex){
        viewPager.setCurrentItem(currentPageIndex);
        prevBtn.setEnabled(currentPageIndex > 0);
        nextBtn.setEnabled(currentPageIndex + 1 < renderer.getPageCount());
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            if (renderer != null && currentPage != null) {
                switch (view.getId()) {
                    case R.id.btnNext: {
                        updateViewPager(currentPageIndex+1);
                    }
                    break;
                    case R.id.btnPrev: {
                        updateViewPager(currentPageIndex-1);
                    }
                    break;
                }
            }
        }
    };

    public class LoadFiles extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingBar.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Void doInBackground(Void... voids) {
            initRenderer();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getBitmapsFromRenderer();
            loadingBar.setVisibility(View.INVISIBLE);
            if (pdfImagesList!=null)
            { initViewPager();}
            
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (currentPage != null) {
                currentPage.close();
            }

            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            renderer.close();
        }
    }
}