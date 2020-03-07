package com.punarinta.RNSoundLevel;


import android.media.MediaRecorder;
import android.util.Log;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;

public class RNSoundLevelService extends Service {
  private MediaRecorder recorder;
  private Handler handler = new Handler();
  private Runnable runnableCode = new Runnable(){
  
    @Override
    public void run() {
      Log.d('t');
    /*   WritableMap body = Arguments.createMap();
      
      body.putDouble("id", frameId++);
      int amplitude = recorder.getMaxAmplitude();
            
      body.putInt("rawValue", amplitude);
      if (amplitude == 0) {
        body.putInt("value", -160);
      } else {
        body.putInt("value", (int) (20 * Math.log(((double) amplitude) / 32767d)));
      }
      sendEvent(body); */
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
      this.handler.removeCallbacks(this.runnableCode);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    this.handler.post(this.runnableCode);
    Log.d(intent);
    createNotificationChannel();
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Music Strobe")
      .setContentText("Mic ON")
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentIntent(contentIntent)
      .setOngoing(true)
      .build();
    startForeground(SERVICE_NOTIFICATION_ID, notification);
    return START_STICKY;
}

private void createNotificationChannel() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "SOUNDLEVEL", importance);
      channel.setDescription("CHANEL DESCRIPTION");
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
  }
}

}