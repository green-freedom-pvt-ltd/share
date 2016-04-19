package com.sharesmile.share.rfac.fragments;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sharesmile.share.R;

public class OnScreenFragment extends Fragment {

    public static ViewPager viewPager;
    FragmentManager mFragmentManager;
    android.app.FragmentTransaction mFragmentTransaction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_layout, null);
        Button lets_run_btn = (Button) v.findViewById(R.id.btn_lets_run);

        mFragmentManager = getFragmentManager();
        // tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(60);
        viewPager.setPadding(140, 50, 140, 0);


// Enable Scrolling by removing the OnTouchListner


        viewPager.setAdapter(new CollectionPagerAdapter(getChildFragmentManager()));

        lets_run_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.drawerLayout, new CauseInfoFragment()).addToBackStack("tag").commit();
            }
        });


        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
      /*      @Override
            public void transformPage(View page, float position) {
                final float normalizedposition = Math.abs(Math.abs(position) - 1);
                //  page.setAlpha(normalizedposition);
                page.setScaleX(normalizedposition / 2 + 0.5f);
                page.setScaleY(normalizedposition / 2 + 0.5f);
                } */

       /*     @Override
            public void transformPage(View page, float position) {
              //  Log.e("pos",new Gson().toJson(position));
                if (position < -1) {
                    page.setScaleY(0.7f);
                    page.setAlpha(1);
                } else if (position <= 1 && position >-1) {
                    float scaleFactor = Math.max(0.7f, 1 - Math.abs(position - 0.14285715f));
                    page.setScaleX(scaleFactor);
                //    Log.e("scale",new Gson().toJson(scaleFactor));
                    page.setScaleY(scaleFactor);
                    page.setAlpha(scaleFactor);
                } else {
                    page.setScaleY(0.7f);
                    page.setAlpha(1);
                }
            } */

            /*        private static final float MIN_SCALE_DEPTH = 0.75f;

                    @Override
                    public void transformPage(View page, float position) {
                        final float alpha;
                        final float scale;
                        final float translationX;


                        if (position > 0 && position < 1) {
                            alpha = (1 - position);
                            scale = MIN_SCALE_DEPTH + (1 - MIN_SCALE_DEPTH) * (1 - Math.abs(position));
                            translationX = (page.getWidth() * -position);
                        } else {
                            alpha = 1;
                            scale = 1;
                            translationX = 0;
                        }

                        page.setAlpha(alpha);
                        page.setTranslationX(translationX);
                        page.setScaleX(scale);
                        page.setScaleY(scale);


                    } */

                    @Override
                    public void transformPage(View page, float position) {
                        int pageWidth = viewPager.getMeasuredWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight();
                        int pageHeight = viewPager.getHeight();
                        int paddingLeft = viewPager.getPaddingLeft();
                        float transformPos = (float) (page.getLeft() - (viewPager.getScrollX() + paddingLeft)) / pageWidth;

                        final float normalizedposition = Math.abs(Math.abs(transformPos) - 1);
                        page.setAlpha(normalizedposition + 0.5f);

                        int max = -pageHeight / 10;

                        if (transformPos < -1) { // [-Infinity,-1)
                            // This page is way off-screen to the left.
                            ;
                            // ObjectAnimator anim = ObjectAnimator.ofFloat(this,"translationX", 0,400);
                            //anim.start();


                        } else if (transformPos <= 1) { // [-1,1]
                            page.setTranslationY(0);


                        } else { // (1,+Infinity]
                            // This page is way off-screen to the right.
                            //ObjectAnimator anim = ObjectAnimator.ofFloat(this,"translationX", 0,400);
                            //anim.start();


                        }


                    }
                }
        );

        return v;

    }

    class CollectionPagerAdapter extends FragmentStatePagerAdapter {


        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new CauseSwipeFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(CauseSwipeFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}