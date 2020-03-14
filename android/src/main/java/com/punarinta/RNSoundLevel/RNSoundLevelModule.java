package com.punarinta.RNSoundLevel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Build;
import android.util.Log;
import android.content.Intent;
import android.content.BroadcastReceiver;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


class RNSoundLevelModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNSoundLevel";
    private static final String CHANNEL_ID = "MusicStrobe";
    private LocalBroadcastReceiver mLocalBroadcastReceiver;
    private Promise promise;
    private boolean hasPromise = false;

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            WritableMap body = Arguments.createMap();
            int value = intent.getIntExtra("value", -100);
            body.putInt("value", intent.getIntExtra("value", -100));
            boolean error = intent.getBooleanExtra("error", false);
            if(!error) {
                if(value != -130){
                    promise.resolve(true);
                    Log.d("ReactNative", String.format("value: %d", value));
                    getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("frame", body);
                }
            } else {
                logAndRejectPromise(promise, intent.getStringExtra("errorCode"),
                        intent.getStringExtra("errorMessage"));
            }
            hasPromise = false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public RNSoundLevelModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);
        localBroadcastManager.registerReceiver(mLocalBroadcastReceiver, new IntentFilter("toModule"));
        createNotificationChannel();
    }

    @Override
    public String getName() {
        return "RNSoundLevelModule";
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void start(Promise promise) {
        if(!hasPromise) {
            this.promise = promise;
            getReactApplicationContext().startService(new Intent(getReactApplicationContext(), RNSoundLevelService.class));
        }
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void stop(Promise promise) {
        getReactApplicationContext().stopService(new Intent(getReactApplicationContext(), RNSoundLevelService.class));
        promise.resolve(true);
    }

    private void logAndRejectPromise(Promise promise, String errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);
        promise.reject(errorCode, errorMessage);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MusicStrobe", importance);
            NotificationManager notificationManager = getReactApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
