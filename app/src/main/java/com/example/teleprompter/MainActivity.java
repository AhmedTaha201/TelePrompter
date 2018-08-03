package com.example.teleprompter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    OnBackPressedListener mOnBackPressedListener;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
    }

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener.isSheetVisible()) {
            mOnBackPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public void setOnBackPressedListener(OnBackPressedListener mOnBackPressedListener) {
        this.mOnBackPressedListener = mOnBackPressedListener;
    }

    public interface OnBackPressedListener {
        boolean isSheetVisible();

        void onBackPressed();
    }
}
