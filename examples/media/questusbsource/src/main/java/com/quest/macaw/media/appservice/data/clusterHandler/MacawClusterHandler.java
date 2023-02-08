package com.quest.macaw.media.appservice.data.clusterHandler;

import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.property.CarPropertyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.quest.macaw.media.appservice.data.volume.MacawAudioManager;
import com.quest.macaw.media.appservice.data.MediaServiceManager;
import com.quest.macaw.media.common.MediaInterfaceConstant;

public class MacawClusterHandler {

    private static final String TAG = "MacawClusterHandler";

    public MacawClusterHandler(Context context, MediaServiceManager mediaServiceManager) {
        int res = context.checkCallingOrSelfPermission("android.car.permission.CAR_VENDOR_EXTENSION");
        Log.i(TAG, "MACAW-vhal Constructor CAR_VENDOR_EXTENSION permission" + (res == PackageManager.PERMISSION_GRANTED));
        CarPropertyManager mCarPropertyManager = (CarPropertyManager) Car.createCar(context).getCarManager(Car.PROPERTY_SERVICE);
        // mCarPropertyManager.registerCallback(new CarPropertyManager.CarPropertyEventCallback() {
        //     boolean isStart = true;
        //     @Override
        //     public void onChangeEvent(CarPropertyValue carPropertyValue) {
        //         Log.i(TAG, "MACAW-vhal onChangeEvent: property MUSIC_TRACK_CONTROL " + carPropertyValue.getValue());
        //         if(!isStart) {
        //             if ((boolean) (carPropertyValue.getValue())) {
        //                 mediaServiceManager.onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_NEXT);
        //             } else {
        //                 mediaServiceManager.onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PREVIOUS);
        //             }
        //         }
        //         isStart = false;
        //     }
        //
        //     @Override
        //     public void onErrorEvent(int i, int i1) {
        //         Log.i(TAG, "MACAW-vhal onErrorEvent: i " + i + " i1 " + i1);
        //     }
        // }, VehiclePropertyIds.MUSIC_TRACK_CONTROL, CarPropertyManager.SENSOR_RATE_NORMAL);


        res = context.checkCallingOrSelfPermission("android.car.permission.CAR_CONTROL_AUDIO_VOLUME");
        Log.i(TAG, "MACAW-vhal Constructor android.car.permission.CAR_CONTROL_AUDIO_VOLUME permission"
                + (res == PackageManager.PERMISSION_GRANTED));

        MacawAudioManager macawAudioManager = new MacawAudioManager(context);
        // mCarPropertyManager.registerCallback(new CarPropertyManager.CarPropertyEventCallback() {
        //     boolean isStart = true;
        //     @Override
        //     public void onChangeEvent(CarPropertyValue carPropertyValue) {
        //         Log.i(TAG, "MACAW-vhal onChangeEvent: property VOLUME_CHANGE "
        //                 + carPropertyValue.getValue());
        //         if(!isStart) {
        //             if ((boolean) (carPropertyValue.getValue())) {
        //                 macawAudioManager.increaseVolume();
        //             } else {
        //                 macawAudioManager.decreaseVolume();
        //             }
        //         }
        //         isStart = false;
        //     }
        //
        //     @Override
        //     public void onErrorEvent(int i, int i1) {
        //         Log.i(TAG, "MACAW-vhal onErrorEvent: i " + i + " i1 " + i1);
        //     }
        // }, VehiclePropertyIds.VOLUME_CHANGE, CarPropertyManager.SENSOR_RATE_NORMAL);
    }
}
