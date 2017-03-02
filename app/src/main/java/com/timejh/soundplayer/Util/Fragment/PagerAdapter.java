package com.timejh.soundplayer.Util.Fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tokijh on 2017. 2. 27..
 */

public class PagerAdapter extends FragmentStatePagerAdapter {

    List<Fragment> fragments;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
    }

    public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public void add(Fragment fragment) {
        fragments.add(fragment);
        this.notifyDataSetChanged();
    }

    public void set(List<Fragment> fragments) {
        this.fragments = fragments;
        this.notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
