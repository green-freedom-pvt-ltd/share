package com.sharesmile.share.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari1 on 27/02/16.
 */
public class CardViewPager extends ViewPager{

    private static final String TAG = "CardViewPager";

    private static final int MIN_DISTANCE_FOR_FLING = 0; // dips
    private static final int DEFAULT_GUTTER_SIZE = 50; // dips
    private static final int MIN_FLING_VELOCITY = 0; // dips

    private static final long HEAP_MEMORY_THRESHOLD = 256 * 1024 * 1024L;
    private static final int OFFSCREEN_PAGE_LIMIT_LOW_END = 1;
    private static final int OFFSCREEN_PAGE_LIMIT_HIGH_END = 2;
    private float downX, downY;
    private long downTime;

    private float gutterSizeDP, minDistanceDP, minFlingVelocityDP;

    public CardViewPager(Context context) {
        super(context);
    }

    public CardViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


}

