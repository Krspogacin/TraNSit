package org.mad.transit.adapters;

import android.content.Context;

import org.mad.transit.R;
import org.mad.transit.fragments.DirectionsFragment;
import org.mad.transit.fragments.TabFragment;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;

    public TabAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given tab.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) { //TODO maybe better use list of fragments and fill it once
            case 0:
                return DirectionsFragment.newInstance();
            case 1:
                return TabFragment.newInstance(position + 1);
            case 2:
                return TabFragment.newInstance(position + 1);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show number of tabs.
        return TAB_TITLES.length;
    }
}