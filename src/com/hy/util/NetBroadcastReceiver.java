package com.hy.util;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetBroadcastReceiver extends BroadcastReceiver {

	public static ArrayList<netEventHandler> mListeners = new ArrayList<netEventHandler>();
	private static String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(NET_CHANGE_ACTION)) {
            NetApplication.mNetWorkState = NetUtil.getNetworkState(context);
            if (mListeners.size() > 0)// ֪ͨ�ӿ���ɼ���
                for (netEventHandler handler : mListeners) {
                    handler.onNetChange();
                }
        }
	}
	public static abstract interface netEventHandler {

        public abstract void onNetChange();
    }
}
