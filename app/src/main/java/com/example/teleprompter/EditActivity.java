package com.example.teleprompter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivity extends AppCompatActivity {

    private onBackPressedListener mOnBackPressedListener;

    @BindView(R.id.edit_toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        // FIXME: 7/23/2018 -- It does not return if it`s just a new empty file
        if (mOnBackPressedListener != null) mOnBackPressedListener.onBackPressed();
    }

    public void setOnBackPressedListener(onBackPressedListener mOnBackPressedListener) {
        this.mOnBackPressedListener = mOnBackPressedListener;
    }

    public interface onBackPressedListener {
        void onBackPressed();
    }
}
