package com.radix.nowplayinglog.service;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;

public class ListenerService extends NotificationListenerService {

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
      String notificationTitle = (String) sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
      Song song = new Song(notificationTitle, sbn.getPostTime());

      // Now store it
      mSongStorage.storeSong(song);

      // Now notify people that storage has been updated
      
    }
  }
}
