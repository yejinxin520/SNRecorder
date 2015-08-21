package com.hy.util;

import android.app.Application;

public class NetApplication extends Application {
	private static Application mApplication;
    public static int mNetWorkState;

    public static synchronized Application getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        initData();
    }



    public void initData() {
        mNetWorkState = NetUtil.getNetworkState(this);
    }
}
