package com.example.teleprompter;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import com.gordonwong.materialsheetfab.AnimatedFab;

//https://github.com/gowong/material-sheet-fab
public class CustomFloatingActionButton extends FloatingActionButton implements AnimatedFab {


    public CustomFloatingActionButton(Context context) {
        super(context);
    }

    public CustomFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show() {
        super.show();
        show(0, 0);
    }

    @Override
    public void show(float translationX, float translationY) {
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        super.hide();
        setVisibility(INVISIBLE);
    }
}
