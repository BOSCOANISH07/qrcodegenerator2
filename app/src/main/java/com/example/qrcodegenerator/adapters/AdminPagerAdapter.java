package com.example.qrcodegenerator.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.qrcodegenerator.fragments.QRCodeListFragment;
import com.example.qrcodegenerator.fragments.UserListFragment;

public class AdminPagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_TABS = 2;

    public AdminPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new UserListFragment();
            case 1:
                return new QRCodeListFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_TABS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Users";
            case 1:
                return "QR Codes";
            default:
                return null;
        }
    }
}