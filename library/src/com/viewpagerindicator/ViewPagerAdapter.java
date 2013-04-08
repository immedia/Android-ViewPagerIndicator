package com.viewpagerindicator;

public interface ViewPagerAdapter {
    /**
     * Get icon representing the page at {@code index} in the adapter.
     */
    int getViewResId(int index);

    // From PagerAdapter
    int getCount();
}
