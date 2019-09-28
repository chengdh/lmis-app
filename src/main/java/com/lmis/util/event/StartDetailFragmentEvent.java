package com.lmis.util.event;

import android.support.v4.app.Fragment;

//打开明细fragment事件
public class StartDetailFragmentEvent {
    public StartDetailFragmentEvent(Fragment mFragment) {
        this.mFragment = mFragment;
    }

    public Fragment getmFragment() {
        return mFragment;
    }

    public void setmFragment(Fragment mFragment) {
        this.mFragment = mFragment;
    }

    private Fragment mFragment;
}
