package com.radix.nowplayinglog.service;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;

public class ListenerService extends NotificationListenerService {
  private static String TAG = ListenerService.class.getName();
  private SongStorageThing mSongStorage;

  @Override
  public void onCreate() {
    super.onCreate();
    mSongStorage = new SongStorageThing(getApplicationContext());
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    super.onNotificationPosted(sbn);

    if (sbn.getPackageName().equals(Constants.NOW_PLAYING_PACKAGE)) {
      Log.d(TAG, "now playing notif posted!");
      String notificationTitle = (String) sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
      Song song = new Song(notificationTitle, sbn.getPostTime());

      // Now store it
      mSongStorage.storeSong(song);
      Log.d(TAG, "song: " +  song);

      // Now notify people that storage has been updated
      Intent caughtSongIntent = new Intent(Constants.NEW_SONG_BROADCAST_FILTER);
      caughtSongIntent.putExtra(Constants.NEW_SONG_BROADCAST_FILTER_SONG_ID, song.getId());
      LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(caughtSongIntent);
      Log.d(TAG, "broadcasted out song");
    }
  }
}
