package com.punarinta.RNSoundLevel;

import android.content.Context;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.content.Intent;
import android.content.BroadcastReceiver;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;


class RNSoundLevelModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNSoundLevel";
    private LocalBroadcastReceiver mLocalBroadcastReceiver;
    private Promise promise;
    private static ReactApplicationContext mReactContext;
    private boolean hasPromise = false;

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getIntExtra("value", -100);
            boolean error = intent.getBooleanExtra("error", false);
            if(!error) {
                mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("frame", value);
                promise.resolve(true);
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
        mReactContext = reactContext;
        this.mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);
        localBroadcastManager.registerReceiver(mLocalBroadcastReceiver, new IntentFilter("toModule"));
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
            mReactContext.startService(new Intent(mReactContext, RNSoundLevelService.class));
        }
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void stop(Promise promise) {
        if(hasPromise) {
            this.promise = promise;
            mReactContext.stopService(new Intent(mReactContext, RNSoundLevelService.class));
        }
    }

    @ReactMethod
    public void startService() {
        mReactContext.startService(new Intent(mReactContext, RNSoundLevelService.class));
    }

    @ReactMethod
    public void stopService() {
        mReactContext.stopService(new Intent(mReactContext, RNSoundLevelService.class));
    }

    private void logAndRejectPromise(Promise promise, String errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);
        promise.reject(errorCode, errorMessage);
    }
}
