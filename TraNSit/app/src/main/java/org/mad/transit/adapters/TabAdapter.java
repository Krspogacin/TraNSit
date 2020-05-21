package org.mad.transit.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.mad.transit.R;
import org.mad.transit.fragments.DirectionsFragment;
import org.mad.transit.fragments.LinesFragment;
import org.mad.transit.fragments.StopsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private static final List<Fragment> tabFragments = new ArrayList<>();
    private final Context mContext;

    public TabAdapter(Context context, FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;

        tabFragments.add(DirectionsFragment.newInstance());
        tabFragments.add(LinesFragment.newInstance());
        tabFragments.add(StopsFragment.newInstance());
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given tab.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position < 0 || position > tabFragments.size() - 1) {
            return tabFragments.get(0);
        }
        return tabFragments.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show number of tabs.
        return TAB_TITLES.length;
    }
}