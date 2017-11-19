package com.radix.nowplayinglog.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.PermissionUtils;

public class ListenerServiceTb extends NotificationListenerService {
  private static String TAG = ListenerServiceTb.class.getName();
  private SongStorageThing mSongStorage;

  @Override
  public void onCreate() {
    super.onCreate();
    mSongStorage = new SongStorageThing(getApplicationContext());

    Log.d(TAG, "listener created");
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    super.onNotificationPosted(sbn);

    Log.d(TAG, "notif posted");

    if (sbn.getPackageName().equals(Constants.NOW_PLAYING_PACKAGE)) {
      String notificationTitle = (String) sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
      Song song = new Song(notificationTitle, sbn.getPostTime());
      Log.d(TAG, "current song: " +  song);

      if (mSongStorage.isSongRepost(song)) {
        Log.d(TAG, "current song is a repeat, ignoring");
        return;
      }

      // Now store it
      mSongStorage.storeSong(song);

      // Now notify people that storage has been updated
      Intent caughtSongIntent = new Intent(Constants.NEW_SONG_BROADCAST_FILTER);
      caughtSongIntent.putExtra(Constants.NEW_SONG_BROADCAST_FILTER_SONG_ID, song.getId());
      LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(caughtSongIntent);
      Log.d(TAG, "broadcast song");
    }

    Location currentLoc = getLocation();
    Log.d(TAG, "currentLoc retrieved " + (currentLoc == null));
  }

  private Location getLocation() {
    boolean isLocationCollectionSettingEnabled = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext())
        .getBoolean(getApplicationContext().getString(R.string.settings_map_key), false);

    boolean isLocationPermissionGranted = PermissionUtils.isPermissionGranted(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    if (isLocationPermissionGranted && isLocationCollectionSettingEnabled) {
      FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
      @SuppressLint("MissingPermission") final Task<Location> lastLocationTask = locationClient.getLastLocation();

      lastLocationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
          Log.d(TAG, "loca: " + location);
        }
      });

      return null;
    } else {
      return null;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return super.onBind(intent);
  }

  @Override
  public void onListenerConnected() {
    super.onListenerConnected();
    Log.d(TAG, "Connected to listener");
  }

  @Override
  public void onListenerDisconnected() {
    super.onListenerDisconnected();
    Log.d(TAG, "Disconnected from listener");
  }

  private boolean shouldCollectLocation() {
    boolean isLocationCollectionSettingEnabled = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext())
        .getBoolean(getApplicationContext().getString(R.string.settings_map_key), false);

    boolean isLocationPermissionGranted = PermissionUtils.isPermissionGranted(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

    return isLocationCollectionSettingEnabled && isLocationPermissionGranted;
  }
}
