package com.quest.macaw.media.appservice.data.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.quest.macaw.media.appservice.data.interfaces.USBManager;

public class USBBroadcastReceiver extends BroadcastReceiver {

    public static final String INTENT_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
    public static final String INTENT_MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED";

    private static final String TAG = "USBBroadcastReceiver";

    private USBManager mUsbManager;

    /*public USBBroadcastReceiver(USBManager usbManager) {
        this.mUsbManager = usbManager;
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            if (INTENT_MEDIA_MOUNTED.equals(intent.getAction())) {
                /* if (mUsbManager != null) {*/
                Log.e(TAG, "onMount");
                    /*mUsbManager.onMount();
                } else {
                    Log.e(TAG, "mUsbManager is null");
                }*/
            } else if (INTENT_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                /*if (mUsbManager != null) {*/
                Log.e(TAG, "onUnMount");
                    /*mUsbManager.onUnMount();
                 } else {
                    Log.e(TAG, "mUsbManager is null");
                }*/
            }
        } else {
            Log.e(TAG, "either intent or action is null");
        }
    }
}