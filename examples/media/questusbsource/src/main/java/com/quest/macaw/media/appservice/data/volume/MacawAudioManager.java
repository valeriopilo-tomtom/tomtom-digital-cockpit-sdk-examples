package com.quest.macaw.media.appservice.data.volume;

import android.car.Car;
import android.car.media.CarAudioManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.quest.macaw.media.appservice.R;


import java.util.Timer;
import java.util.TimerTask;

public class MacawAudioManager {
    private static final String TAG = MacawAudioManager.class.getSimpleName();
    private static final int DEFAULT_VOL_GROUP_ID = 0;
    private final CarAudioManager mCarAudioManager;
    Context mContext;
    private Handler mHandler;

    private final Runnable mDialogDismissRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "MACAW-vhal handler run: ");
            try {
                if(mVolumeBar != null) {
                    mWindowManager.removeView(mVolumeBar);
                    mVolumeBar = null;
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "MACAW-vhal handler Error dismissing");
            }
        }
    };
    private ImageView volumeImg;
    private int mVolume;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private View mVolumeBar;

    final CarAudioManager.CarVolumeCallback mVolumeChangeCallback =
            new CarAudioManager.CarVolumeCallback() {
                @Override
                public void onGroupVolumeChanged(int zoneId, int groupId, int flags) {
                    if (mCarAudioManager != null) {
                        mVolume = mCarAudioManager.getGroupVolume(groupId);
                        Log.i(TAG, "MACAW-vhal onGroupVolumeChanged: zoneId " + zoneId
                                + " groupId " + groupId
                                + " volume " + mVolume);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(mVolumeBar != null) {
                                    updateVolumeImage();
                                } else {
                                    createVolumeBar();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onMasterMuteChanged(int zoneId, int flags) {
                    // Mute is not being used yet
                }
            };

    public MacawAudioManager(Context context) {
        mContext = context;
        mCarAudioManager = (CarAudioManager) Car.createCar(context)
                .getCarManager(Car.AUDIO_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        mCarAudioManager.registerCarVolumeCallback(mVolumeChangeCallback);
        Log.i(TAG, "MACAW-vhal MacawAudioManager: current volume " + mCarAudioManager.getGroupVolume(DEFAULT_VOL_GROUP_ID)
                + " max volume " + mCarAudioManager.getGroupMaxVolume(DEFAULT_VOL_GROUP_ID)
                + " min volume " + mCarAudioManager.getGroupMinVolume(DEFAULT_VOL_GROUP_ID));
        initVoiceChromeDialogPopup(context);
    }

    private void createVolumeBar() {
        // mVolumeBar = View.inflate(mContext.getApplicationContext(), R.layout.volume_fragment, null);
        // mWindowManager.addView(mVolumeBar, mWindowParams);
        // volumeImg = mVolumeBar.findViewById(R.id.icon);
        // updateVolumeImage();
        // mHandler.removeCallbacks(mDialogDismissRunnable);
        // mHandler.postDelayed(mDialogDismissRunnable, 2000);
    }

    void updateVolumeImage() {
        Log.i(TAG, "MACAW-vhal updateVolumeImage:" + mVolume);
        switch (mVolume) {
            case 0:
                volumeImg.setImageDrawable(mContext.getDrawable(R.drawable.volume_level_0));
                break;
            case 1:
                volumeImg.setImageDrawable(mContext.getDrawable(R.drawable.volume_level_1));
                break;
            case 2:
                volumeImg.setImageDrawable(mContext.getDrawable(R.drawable.volume_level_2));
                break;
            case 3:
                volumeImg.setImageDrawable(mContext.getDrawable(R.drawable.volume_level_3));
                break;
            case 4:
                volumeImg.setImageDrawable(mContext.getDrawable(R.drawable.volume_level_4));
                break;
            case 5:
                volumeImg.setImageDrawable(mContext.getDrawable(R.drawable.volume_level_5));
                break;
        }
    }


    private void initVoiceChromeDialogPopup(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        mWindowParams.width =  WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.x = 0;
        mWindowParams.y = 0;
        mWindowParams.gravity = Gravity.CENTER_HORIZONTAL;
        mWindowParams.windowAnimations = android.R.style.Animation_Dialog;
    }

    public void increaseVolume() {
        int currentVolume = mCarAudioManager.getGroupVolume(DEFAULT_VOL_GROUP_ID);
        int maxVolume = mCarAudioManager.getGroupMaxVolume(DEFAULT_VOL_GROUP_ID);
        if (currentVolume < maxVolume) {
            Log.i(TAG, "MACAW-vhal increaseVolume: " + (currentVolume + 1));
            mCarAudioManager.setGroupVolume(DEFAULT_VOL_GROUP_ID, (currentVolume + 1), 0);
        } else {
            Log.i(TAG, "MACAW-vhal Volume at max");
            mCarAudioManager.setGroupVolume(DEFAULT_VOL_GROUP_ID, maxVolume, 0);
        }
    }

    public void decreaseVolume() {
        int currentVolume = mCarAudioManager.getGroupVolume(DEFAULT_VOL_GROUP_ID);
        int minVolume = mCarAudioManager.getGroupMinVolume(DEFAULT_VOL_GROUP_ID);
        if (currentVolume > minVolume) {
            Log.i(TAG, "MACAW-vhal decreaseVolume: " + (currentVolume - 1));
            mCarAudioManager.setGroupVolume(DEFAULT_VOL_GROUP_ID, (currentVolume - 1), 0);
        } else {
            mCarAudioManager.setGroupVolume(DEFAULT_VOL_GROUP_ID, minVolume, 0);
            Log.i(TAG, "MACAW-vhal Volume at min");
        }
    }
}
