package com.example.pdfviewerapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pdfviewerapp.Adapters.PdfViewPager2Adapter;
import com.example.pdfviewerapp.R;
import com.example.pdfviewerapp.constants.Constant;
import com.example.pdfviewerapp.dialogboxes.NumberPickerDialog;
import com.lukelorusso.verticalseekbar.VerticalSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PdfViewActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    String TAG = "ActivityPdfRenderView";
    private PdfRenderer renderer;
    private PdfRenderer.Page currentPage;
    private LinearLayout rootViewChangeButton;
    private ProgressBar loadingBar;
    private Button prevBtn;
    private Button nextBtn;
    private TextView tv_pageCount;
    private ImageButton btnFindPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private ViewPager2 viewPager;
    private PdfViewPager2Adapter pagerAdapter;
    private ArrayList<Bitmap> pdfImagesList;
    private int currentPageIndex;
    private int totalPages;
    private VerticalSeekBar verticalSeekBar;
    private boolean isSwipe;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSharedPrefs();
        if (getThemeNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_pdf_view);
        Intent intent = getIntent();
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        rootViewChangeButton = (LinearLayout) findViewById(R.id.pdfView_ac_rootView_buttons);
        tv_pageCount = (TextView) findViewById(R.id.pdfView_ac_pageCount_Tv);
        verticalSeekBar = findViewById(R.id.pdfView_ac_seekbarVertical);
        prevBtn = (Button) findViewById(R.id.btnPrev);
        nextBtn = (Button) findViewById(R.id.btnNext);
        btnFindPage = (ImageButton) findViewById(R.id.btn_findPage);
        btnFindPage.setOnClickListener(onClickListener);
        nextBtn.setOnClickListener(onClickListener);
        prevBtn.setOnClickListener(onClickListener);
        tv_pageCount.setOnClickListener(onClickListener);

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
            totalPages = renderer.getPageCount() - 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "initRenderer: Error File Not Found");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initViewPager() {
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new PdfViewPager2Adapter(this, pdfImagesList, viewPager);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setCurrentItem(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                Log.d(TAG, "Page ChangeListener  Called: with Page No" + currentPageIndex);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "Page SelectedListener  Called:");
                super.onPageSelected(position);
            }


            @Override
            public void onPageScrollStateChanged(int state) {
                currentPageIndex = viewPager.getCurrentItem();
                prevBtn.setEnabled(currentPageIndex > 0);
                nextBtn.setEnabled(currentPageIndex + 1 < totalPages);
                updateScrollerValue(currentPageIndex);
//                updatePageCountTV(currentPageIndex);
                Log.d(TAG, "Page ScrolledState Changed  Called: with state:" + viewPager.getCurrentItem());
                isSwipe = true;

                super.onPageScrollStateChanged(state);
            }
        });

    }



    private void getBitmapsFromRenderer() {
        pdfImagesList = new ArrayList<>();
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

    private void setUpVerticalSeekBar(int maxValue) {
        verticalSeekBar.setMaxValue(maxValue);
        verticalSeekBar.setProgress(0);
        verticalSeekBar.setOnProgressChangeListener(
                new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer progressValue) {
                        updateViewPager(progressValue);
                        isSwipe = false;
                        Log.d(TAG, "seekbar Pressed Called:");
                        return null;
                    }
                }
        );
        verticalSeekBar.setOnPressListener(
                new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer progressValue) {
                        if (isSwipe == false) {
                            updateViewPager(progressValue);
                            Log.d(TAG, "seekbar Value Change Called:");
                        }
                        isSwipe = false;
                        return null;
                    }
                }
        );
        verticalSeekBar.setOnReleaseListener(
                new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer progressValue) {
                        updateViewPager(progressValue);
                        Log.d(TAG, "seekbar Release  Called:");
                        return null;
                    }
                }
        );

    }

    private void initSharedPrefs() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
    }

    private boolean getThemeNightMode() {
        return sharedPreferences.getBoolean(Constant.KEY_PREFS_THEME_MODE, false);
    }

    private void saveThemeNightMode(boolean nightMode) {
        editor.putBoolean(Constant.KEY_PREFS_THEME_MODE, nightMode);
        editor.commit();
        editor.apply();
    }

    private void setThemeMode(boolean isNightMode) {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_pdfview, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        MenuItem menuIsNightMode = menu.findItem(R.id.menu_pdfView_item_Theme);
        if (getThemeNightMode()) {
            menuIsNightMode.setChecked(true);
            setThemeMode(true);
        } else {
            menuIsNightMode.setChecked(false);
            setThemeMode(false);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pdfView_item_Theme: {
                if (item.isChecked()) {
                    item.setChecked(false);
                    saveThemeNightMode(false);
                    setThemeMode(false);
                } else {
                    item.setChecked(true);
                    saveThemeNightMode(true);
                    setThemeMode(true);
                }
            }
            break;
            case R.id.menu_pdfView_item_Vertical: {
                setViewPagerOrientation(ViewPager2.ORIENTATION_VERTICAL);
                if (rootViewChangeButton.getVisibility() == View.VISIBLE)
                    rootViewChangeButton.setVisibility(View.INVISIBLE);
                verticalSeekBar.setVisibility(View.VISIBLE);
            }
            break;
            case R.id.menu_pdfView_item_Horizontal: {
                setViewPagerOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                if (verticalSeekBar.getVisibility() == View.VISIBLE) {
                    rootViewChangeButton.setVisibility(View.VISIBLE);
                    verticalSeekBar.setVisibility(View.INVISIBLE);

                }
            }
            break;
            default: {
                super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    private void setViewPagerOrientation(int orientation) {

        viewPager.setOrientation(orientation);
    }

    private void updateViewPager(int currentPageIndex) {
        viewPager.setCurrentItem(currentPageIndex);
        prevBtn.setEnabled(currentPageIndex > 0);
        nextBtn.setEnabled(currentPageIndex + 1 < totalPages);
        updatePageCountTV(currentPageIndex);
    }
    private void updatePageCountTV(int currentPageIndex) {
        tv_pageCount.setText(currentPageIndex + "/" + totalPages);
    }

    private void updateScrollerValue(int progress) {
        if (isSwipe == true) {
            verticalSeekBar.setProgress(progress);
        }
    }

    public void showNumberPicker() {
        NumberPickerDialog newFragment = new NumberPickerDialog(0, renderer.getPageCount()-1);
        newFragment.setValueChangeListener(this);
        newFragment.show(getSupportFragmentManager(), "time picker");
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            if (renderer != null && currentPage != null) {
                switch (view.getId()) {
                    case R.id.btnNext: {
                        updateViewPager(currentPageIndex + 1);
                    }
                    break;
                    case R.id.btnPrev: {
                        updateViewPager(currentPageIndex - 1);
                    }
                    break;
                    case R.id.btn_findPage: {
                        showNumberPicker();
                    }
                    break;
                    case R.id.pdfView_ac_pageCount_Tv: {
                        showNumberPicker();
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

        updateViewPager(i);

        Log.d(TAG, "onValueChange: Number PickerValue:" + numberPicker.getValue() + "i:" + i + "i1:" + i);
    }


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
            if (pdfImagesList != null) {
                tv_pageCount.setVisibility(View.VISIBLE);
                initViewPager();
                updatePageCountTV(0);
                setUpVerticalSeekBar(totalPages);
            }

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