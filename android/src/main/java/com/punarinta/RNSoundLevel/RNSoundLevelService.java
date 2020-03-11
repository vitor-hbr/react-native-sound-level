package com.punarinta.RNSoundLevel;

import android.media.MediaRecorder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import android.app.Notification;
import android.app.PendingIntent;
import com.facebook.react.bridge.Arguments;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNSoundLevelService extends Service {
  private MediaRecorder recorder;
  private boolean isRecording = false;
  private ReactApplicationContext context;
  private Handler handler = new Handler();
  private Runnable runnableCode = new Runnable() {

    @Override
    public void run() {
      int value;

      int amplitude = recorder.getMaxAmplitude();

      if (amplitude == 0) {
        value = -160;
      } else {
        value = (int) (20 * Math.log(((double) amplitude) / 32767d));
      }
      sendEvent(value);
      handler.postAtTime(this, 100);
    }
  };

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (!isRecording) {
      sendEventError(true, "INVALID_STATE", "Please call start before stopping recording");
    }
    isRecording = false;

    try {
      recorder.stop();
      recorder.release();
    } catch (final RuntimeException e) {
      sendEventError(true, "RUNTIME_EXCEPTION", "No valid audio data received. You may be using a device that can't record audio.");
    } finally {
      recorder = null;
    }
    
    this.handler.removeCallbacks(this.runnableCode);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (isRecording) {
      sendEventError(true, "INVALID_STATE", "Please call stop before starting");
    } else {
      recorder = new MediaRecorder();
      try {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(22050);
        recorder.setAudioChannels(1);
        recorder.setAudioEncodingBitRate(32000);
        recorder.setOutputFile(this.getApplicationContext().getCacheDir().getAbsolutePath() + "/soundlevel");
      } catch (final Exception e) {
        sendEventError(true,"COULDNT_CONFIGURE_MEDIA_RECORDER", "Make sure you've added RECORD_AUDIO " +
                "permission to your AndroidManifest.xml file " + e.getMessage());
        return 	START_NOT_STICKY;
      }
      try {
        recorder.prepare();
      } catch (final Exception e) {
        sendEventError(true, "COULDNT_PREPARE_RECORDING", e.getMessage());
        return 	START_NOT_STICKY;
      }

      recorder.start();

      isRecording = true;

      this.handler.post(this.runnableCode);
      /*
       * createNotificationChannel(); Intent notificationIntent = new Intent(this,
       * MainActivity.class); PendingIntent contentIntent =
       * PendingIntent.getActivity(this, 0, notificationIntent,
       * PendingIntent.FLAG_CANCEL_CURRENT); Notification notification = new
       * NotificationCompat.Builder(this, CHANNEL_ID) .setContentTitle("Music Strobe")
       * .setContentText("Mic ON") //.setSmallIcon(R.mipmap.ic_launcher)
       * .setContentIntent(contentIntent) .setOngoing(true) .build();
       * startForeground(SERVICE_NOTIFICATION_ID, notification);
       */
      return START_STICKY;
    }
    return 	START_NOT_STICKY;
  }

  private void sendEvent(int value) {
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    Intent customEvent = new Intent("toModule");
    customEvent.putExtra("value", value);
    localBroadcastManager.sendBroadcast(customEvent);
  }

  private void sendEventError(boolean error, String errorCode, String errorMessage) {
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    Intent customEvent = new Intent("toModule");
    customEvent.putExtra("error", error);
    customEvent.putExtra("errorCode", errorCode);
    customEvent.putExtra("errorMessage", errorMessage);
    localBroadcastManager.sendBroadcast(customEvent);
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel("SOUNDLEVEL", "SOUNDLEVEL", importance);
      channel.setDescription("CHANEL DESCRIPTION");
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

}