package com.example.teleprompter.customview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import timber.log.Timber;

/* Resources for the CustomScrollView
 * https://stackoverflow.com/questions/19144961/detect-end-of-fling-on-scrollview
 * https://stackoverflow.com/questions/8181828/android-detect-when-scrollview-stops-scrolling
 * https://stackoverflow.com/questions/3948934/synchronise-scrollview-scroll-positions-android */

public class CustomScrollView extends ScrollView {

    private static final int DELAY_MILLIS = 100;

    private boolean mBottomReached;

    boolean mFlingRunning;
    boolean mAnimateAfterFling;

    private ObjectAnimator mObjectAnimator;

    public interface OnScrollListener {

        void onFlingStarted();

        void onFlingStopped();
    }

    private OnScrollListener mScrollListener;
    private Runnable mScrollChecker;
    private int mPreviousPosition;

    public CustomScrollView(Context context) {
        this(context, null, 0);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);

        mScrollChecker = new Runnable() {
            @Override
            public void run() {
                int position = getScrollY();
                if (mPreviousPosition - position == 0) {
                    mFlingRunning = false;
                    mScrollListener.onFlingStopped();
                    removeCallbacks(mScrollChecker);
                } else {
                    mPreviousPosition = getScrollY();
                    postDelayed(mScrollChecker, DELAY_MILLIS);
                }
            }
        };
    }

    @Override
    public void fling(int velocityY) {
        if (mObjectAnimator != null) mObjectAnimator.cancel();
        super.fling(velocityY);
        mFlingRunning = true;
        if (mScrollListener != null) {
            mScrollListener.onFlingStarted();
            post(mScrollChecker);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        View view = getChildAt(0);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));
        if (diff == 0) {
            mBottomReached = true;
            Timber.d("Reached the bottom");
        }
        if (diff > 0 && mBottomReached) {
            mBottomReached = false;
            Timber.d("Reached the bottom and got up again");
        }

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setOnScrollListener(OnScrollListener mOnScrollListener) {
        this.mScrollListener = mOnScrollListener;
    }

    public void setObjectAnimator(ObjectAnimator mObjectAnimator) {
        this.mObjectAnimator = mObjectAnimator;
    }

    public boolean isBottomReached() {
        return mBottomReached;
    }

    public boolean isFlingRunning() {
        return mFlingRunning;
    }

    public boolean isAnimateAfterFling() {
        return mAnimateAfterFling;
    }

    public void setAnimateAfterFling(boolean mAnimateAfterFling) {
        this.mAnimateAfterFling = mAnimateAfterFling;
    }
}
