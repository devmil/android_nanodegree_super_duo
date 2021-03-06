package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PagerFragment extends Fragment
{
    public static final String ARG_INITIAL_FRAGMENT_OFFSET = "INITIAL_FRAGMENT_OFFSET";

    private static final String KEY_ACTIVE_FRAGMENT = "ACTIVE_FRAGMENT";

    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager_fragment_pager);
        MyPageAdapter mPagerAdapter = new MyPageAdapter(getChildFragmentManager());
        for (int i = 0;i < NUM_PAGES;i++)
        {
            Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            viewFragments[i] = new MainScreenFragment();
            viewFragments[i].setFragmentDate(mformat.format(fragmentdate));
        }
        mPagerHandler.setAdapter(mPagerAdapter);

        boolean indexInitialized = false;

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(KEY_ACTIVE_FRAGMENT)) {
                mPagerHandler.setCurrentItem(savedInstanceState.getInt(KEY_ACTIVE_FRAGMENT));
                indexInitialized = true;
            }
        }
        if(!indexInitialized
            && getArguments() != null
            && getArguments().containsKey(ARG_INITIAL_FRAGMENT_OFFSET)) {

            mPagerHandler.setCurrentItem(2 + getArguments().getInt(ARG_INITIAL_FRAGMENT_OFFSET));
            indexInitialized = true;
        }

        if(!indexInitialized) {
            mPagerHandler.setCurrentItem(0);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null) {
            outState.putInt(KEY_ACTIVE_FRAGMENT, mPagerHandler.getCurrentItem());
        }
    }

    private class MyPageAdapter extends FragmentStatePagerAdapter
    {
        @Override
        public Fragment getItem(int i)
        {
            return viewFragments[i];
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public MyPageAdapter(FragmentManager fm)
        {
            super(fm);
        }
        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position)
        {
            return Utilities.getDayName(getActivity(),System.currentTimeMillis()+((position-2)*86400000));
        }

    }
}
