package org.mad.transit.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.mad.transit.R;
import org.mad.transit.fragments.TimetableFragment;
import org.mad.transit.model.TimetableDay;

import java.util.Calendar;

public class TimetableTabAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.timetable_tab1, R.string.timetable_tab2, R.string.timetable_tab3};
    private final Context mContext;

    public TimetableTabAdapter(Context context, FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 2:
                return TimetableFragment.newInstance(TimetableDay.SUNDAY.toString());
            case 1:
                return TimetableFragment.newInstance(TimetableDay.SATURDAY.toString());
            default:
                return TimetableFragment.newInstance(TimetableDay.WORKDAY.toString());
        }
    }

    public int getTabIndexByDayInWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return 2;
            case Calendar.SATURDAY:
                return 1;
            default:
                return 0;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}
