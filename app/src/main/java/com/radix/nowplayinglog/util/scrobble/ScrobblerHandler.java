package com.radix.nowplayinglog.util.scrobble;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.util.Constants;

/**
 * Scrobbles things, idk
 */
public class ScrobblerHandler {
  private static final String TAG = ScrobblerHandler.class.getName();

  private final Context mContext;

  public ScrobblerHandler(Context context) {
    mContext = context;
  }

  public void sendScrobbleRequest(Song song) {
    try {
      // https://github.com/tgwizard/sls/wiki/Developer's-API
      Intent bCast = new Intent("com.adam.aslfms.notify.playstatechanged");
      bCast.putExtra("state", 0);
      bCast.putExtra("app-name", "Now Playing Log");
      bCast.putExtra("app-package", mContext.getPackageName());
      bCast.putExtra("artist", song.getArtist());
      bCast.putExtra("track", song.getTitle());
      bCast.putExtra("duration", 30);
      mContext.sendBroadcast(bCast);
    } catch (Exception e) {
      Log.e(TAG, "Failed to scrobble", e);
    }
  }

  public void takeUserToScrobbleApp() {
    // from https://stackoverflow.com/a/27032821
    final String appPackageName = Constants.SCROBBLER_PACKAGE_NAME;
    try {
      mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
    } catch (android.content.ActivityNotFoundException anfe) {
      mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
    }
  }

  public boolean shouldScrobble() {
    return isScrobblingEnabled() && isScrobblerAppInstalled();
  }

  public boolean isScrobblerAppInstalled() {
    // from https://stackoverflow.com/a/6758962
    PackageManager pm = mContext.getPackageManager();
    try {
      PackageInfo info = pm.getPackageInfo(Constants.SCROBBLER_PACKAGE_NAME,PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
    return true;
  }

  private boolean isScrobblingEnabled() {
    return PreferenceManager
        .getDefaultSharedPreferences(mContext)
        .getBoolean(mContext.getString(R.string.settings_scrobble_app_key), false);
  }
}
