package org.mad.transit.adapters;

import android.content.Context;

import org.mad.transit.R;
import org.mad.transit.fragments.TimetableFragment;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.TimetableDay;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.view.model.TimetableViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TimetableTabAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.timetable_tab1, R.string.timetable_tab2, R.string.timetable_tab3};
    private static final List<Fragment> tabFragments = new ArrayList<>();
    private final Context mContext;

    public TimetableTabAdapter(Context context, FragmentManager fm, Long lineId, LineDirection lineDirection) {
        super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;

        TimetableViewModel.timetableMap = TimetableRepository.retrieveLineTimetables(mContext.getContentResolver(), lineId, lineDirection);

        tabFragments.add(TimetableFragment.newInstance(TimetableDay.WORKDAY.toString()));
        tabFragments.add(TimetableFragment.newInstance(TimetableDay.SATURDAY.toString()));
        tabFragments.add(TimetableFragment.newInstance(TimetableDay.SUNDAY.toString()));
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
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
        return TAB_TITLES.length;
    }
}
