package com.radix.nowplayinglog.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.PermissionUtils;
import com.radix.nowplayinglog.util.scrobble.ScrobblerHandler;

public class ListenerServiceT extends NotificationListenerService {
  private static String TAG = ListenerServiceT.class.getName();
  private SongStorageThing mSongStorage;
  private ScrobblerHandler mScrobblerHandler;

  @Override
  public void onCreate() {
    super.onCreate();
    mSongStorage = new SongStorageThing(getApplicationContext());

    Log.d(TAG, "listener created");
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    super.onNotificationPosted(sbn);

    if (sbn.getPackageName().equals(Constants.NOW_PLAYING_PACKAGE)) {
      String notificationTitle = (String) sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
      final Song songWithoutLocation = new Song(notificationTitle, sbn.getPostTime(), null);
      Log.d(TAG, "current song, before location: " +  songWithoutLocation);

      if (mSongStorage.isSongRepost(songWithoutLocation)) {
        Log.d(TAG, "current song is a repeat, ignoring");
        return;
      }

      if (shouldCollectLocation()) {
        // Grab the location and recreate the song object
        try {
          collectLocationAndPublishUpdatedSong(songWithoutLocation);
        } catch (Exception e) {
          Log.e(TAG, "Failed to collect location on song, publishing without any location");
          publishSong(songWithoutLocation);
        }
      } else {
        publishSong(songWithoutLocation);
      }
    }
  }

  @SuppressLint("MissingPermission")
  private void collectLocationAndPublishUpdatedSong(final Song existingSong) {
    FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
    final Task<Location> lastLocationTask = locationClient.getLastLocation();

    lastLocationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
      @Override
      public void onSuccess(Location location) {
        Song songWithLocation = new Song(existingSong, location);
        publishSong(songWithLocation);
      }
    });

    lastLocationTask.addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Location collection failed from fused api for song. Publishing the regular version");
        publishSong(existingSong);
      }
    });
  }

  /**
   * Takes a complete song object and publishes it to the list and to storage.
   */
  private void publishSong(Song song) {
    Log.d(TAG, "Broadcasting song: " + song);
    // Now store it
    mSongStorage.storeSong(song);

    // Now notify people that storage has been updated
    Intent caughtSongIntent = new Intent(Constants.NEW_SONG_BROADCAST_FILTER);
    caughtSongIntent.putExtra(Constants.BROADCAST_FILTER_SONG_ID, song.getId());
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(caughtSongIntent);

    // Scrobble it maybe
    if (mScrobblerHandler.shouldScrobble()) {
      mScrobblerHandler.sendScrobbleRequest(song);
    }
    Log.d(TAG, "Finishing Broadcast song");
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
