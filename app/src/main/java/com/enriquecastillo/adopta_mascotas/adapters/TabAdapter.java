package com.enriquecastillo.adopta_mascotas.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.enriquecastillo.adopta_mascotas.ui.post.PostFragment;
import com.enriquecastillo.adopta_mascotas.ui.profile.ProfileFragment;

public class TabAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;

    public TabAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PostFragment();
            case 1:
                return new ProfileFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}