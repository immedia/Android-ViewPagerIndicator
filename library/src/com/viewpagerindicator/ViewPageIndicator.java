/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viewpagerindicator;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * This widget implements the dynamic action bar tab behavior that can change across different configurations or circumstances.
 */
public class ViewPageIndicator extends HorizontalScrollView implements PageIndicator {
    private final IcsLinearLayout mViewLayout;

    private ViewPager mViewPager;
    private OnPageChangeListener mListener;
    private Runnable mViewSelector;
    private int mSelectedIndex;

    public ViewPageIndicator(Context context) {
        this(context, null);
    }

    public ViewPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mViewLayout = new IcsLinearLayout(context, R.attr.vpiViewPageIndicatorStyle);
        addView(mViewLayout, new LayoutParams(WRAP_CONTENT, FILL_PARENT, Gravity.CENTER));
    }

    private void animateToView(final int position) {
        final View view = mViewLayout.getChildAt(position);
        if (mViewSelector != null) {
            removeCallbacks(mViewSelector);
        }
        mViewSelector = new Runnable() {
            public void run() {
                final int scrollPos = view.getLeft() - (getWidth() - view.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mViewSelector = null;
            }
        };
        post(mViewSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewSelector != null) {
            // Re-post the selector we saved
            post(mViewSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mViewSelector != null) {
            removeCallbacks(mViewSelector);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (mListener != null) {
            mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        if (mListener != null) {
            mListener.onPageSelected(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mViewLayout.removeAllViews();
        ViewPagerAdapter viewAdapter = (ViewPagerAdapter) mViewPager.getAdapter();
        int count = viewAdapter.getCount();
        for (int i = 0; i < count; i++) {
            mViewLayout.addView(LayoutInflater.from(getContext()).inflate(viewAdapter.getViewResId(i), null));
        }
        if (mSelectedIndex > count) {
            mSelectedIndex = count - 1;
        }
        setCurrentItem(mSelectedIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mSelectedIndex = item;
        mViewPager.setCurrentItem(item);

        int tabCount = mViewLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            View child = mViewLayout.getChildAt(i);
            boolean isSelected = (i == item);
            child.setSelected(isSelected);
            if (isSelected) {
                animateToView(item);
            }
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }
}
