package com.delphiworlds.kastri;

/*******************************************************
 *                                                     *
 *                  Kastri Free                        *
 *                                                     *
 *         DelphiWorlds Cross-Platform Library         *
 *                                                     *
 *******************************************************/

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import java.io.IOException;
import java.net.URL;

public class DWNotificationPublisher {

  private static final String TAG = "DWNotificationPublisher";
  private static int mUniqueId = 0;
  private static NotificationChannel mDefaultChannel;
  private static NotificationManager mNotificationManager = null;

  private static void initialize(Context context) {
      if (mNotificationManager != null)
        return;
      mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (Build.VERSION.SDK_INT < 26)
        return; 
      mDefaultChannel = new NotificationChannel(context.getPackageName(), "default", 4);
      mDefaultChannel.setName("Default");
      mDefaultChannel.enableLights(true);
      mDefaultChannel.enableVibration(true);
      mDefaultChannel.setLightColor(Color.GREEN);
      mDefaultChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
      mDefaultChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
      mNotificationManager.createNotificationChannel(mDefaultChannel);
  }

  private static void getLargeIcon(URL url, NotificationCompat.Builder builder) throws IOException {
    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
    if (bitmap != null) {
      int width;
      if (bitmap.getWidth() < bitmap.getHeight()) { 
        width = bitmap.getWidth(); }
      else { 
        width = bitmap.getHeight(); 
      }
      Bitmap bitmapCropped = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2, (bitmap.getHeight() - width) / 2, width, width, null, true);
      if (!bitmap.sameAs(bitmapCropped)) 
        bitmap.recycle(); 
      builder = builder.setLargeIcon(bitmapCropped); 
    } 
  }

  public static void sendNotification(Context context, Intent intent, boolean pending) {
    Log.v(TAG, "+sendNotification");
    initialize(context);
    String channelId = "";
    if (Build.VERSION.SDK_INT >= 26) {
      channelId = mDefaultChannel.getId();
      if (intent.hasExtra("android_channel_id"))
        channelId = mNotificationManager.getNotificationChannel(intent.getStringExtra("android_channel_id")).getId();
    }
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
    if (intent.hasExtra("notification_color")) { 
      builder = builder.setColor(Integer.parseInt(intent.getStringExtra("notification_color")));
    }
    String text = null;
    if (intent.hasExtra("notification_text")) { 
      text = intent.getStringExtra("notification_text");
    } else if (intent.hasExtra("body")) {
      text = intent.getStringExtra("body");      
    }
    builder = builder.setContentText(text);
    if (intent.hasExtra("big_text") && (Integer.parseInt(intent.getStringExtra("big_text")) == 1)) {
      Log.v(TAG, "Notification has big text flag");
      builder = builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
    }
    if (intent.hasExtra("notification_title")) { 
      builder = builder.setContentTitle(intent.getStringExtra("notification_title"));
    } else if (intent.hasExtra("title")) { 
      builder = builder.setContentTitle(intent.getStringExtra("title"));
    }
    String smallIconIdent = null;
    if (intent.hasExtra("notification_smallicon")) {
      smallIconIdent = intent.getStringExtra("notification_smallicon");
    }  else if (intent.hasExtra("icon")) { 
      smallIconIdent = intent.getStringExtra("icon");
    }
    int smallIcon = 0;
    //if (smallIconIdent != null)
    //  smallIcon = this.getApplicationContext().getResources().getIdentifier(smallIconIdent, "drawable", this.getApplicationContext().getPackageName());
    if (smallIcon == 0) {
      smallIcon = context.getApplicationInfo().icon;
    }
    builder.setSmallIcon(smallIcon);
    if (intent.hasExtra("notification_largeicon")) { 
      try {
        URL url = new URL(intent.getStringExtra("notification_largeicon"));
        DWNotificationPublisher.getLargeIcon(url, builder);    
      } catch(Throwable e) { 
        Log.e(TAG, "Exception", e); 
      }
    }
    if (intent.hasExtra("notification_onlyalertonce") && intent.getStringExtra("notification_onlyalertonce").equals("1")) { 
      builder = builder.setOnlyAlertOnce(true);
    } 
    if (intent.hasExtra("notification_ticker")) { 
      builder = builder.setTicker(intent.getStringExtra("notification_ticker"));
    }
    if (intent.hasExtra("notification_vibrate") && intent.getStringExtra("notification_vibrate").equals("1")) { 
      builder = builder.setVibrate(new long[] { 0, 1200 });
    } 
    if (intent.hasExtra("notification_visibility")) { 
      builder = builder.setVisibility(Integer.parseInt(intent.getStringExtra("notification_visibility")));
    }
    if (intent.hasExtra("notification_priority")) { 
      builder = builder.setPriority(Integer.parseInt(intent.getStringExtra("notification_priority")));
    } 
    if (pending) {
      intent.setClassName(context, "com.embarcadero.firemonkey.FMXNativeActivity");
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      mUniqueId = mUniqueId + 1;
      PendingIntent pendingIntent = PendingIntent.getActivity(context, mUniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder = builder.setContentIntent(pendingIntent); 
    }
    builder = builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
      .setWhen(System.currentTimeMillis())
      .setShowWhen(true)
      .setAutoCancel(true);
    // if (intent.hasExtra("notification_badgecount")) 
    //  ShortcutBadger.applyCount(this.getApplicationContext(), Integer.parseInt(intent.getStringExtra("notification_badgecount")));
    mNotificationManager.notify(mUniqueId, builder.build());
    Log.v(TAG, "-sendNotification");
  }
}