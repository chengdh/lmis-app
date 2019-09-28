package com.lmis.util.event;

import android.support.v4.app.Fragment;

//打开住fragment事件
public class StartMainFragmentEvent {
    public Fragment getmFragment() {
        return mFragment;
    }

    public StartMainFragmentEvent(Fragment mFragment) {
        this.mFragment = mFragment;
    }

    public void setmFragment(Fragment mFragment) {
        this.mFragment = mFragment;
    }

    private Fragment mFragment;
}
