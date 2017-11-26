package com.radix.nowplayinglog.util.clicking;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;

public class ClickHandlerProvider {
  private static final String TAG = DefaultClickHandler.class.getName();

  private final Context mContext;
  private final SharedPreferences mDefaultSharedPrefs;

  public ClickHandlerProvider(Context context) {
    mContext = context;
    mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public ISongClickHandler getAppropriateHandler() {
    // Check the current chooser value
    String currentPlayerValue = mDefaultSharedPrefs.getString(mContext.getString(R.string.settings_music_player_key), "");
    Log.d(TAG, "Handling song click for " + currentPlayerValue);
    if (currentPlayerValue.equals("spotify")) {
      return new SpotifyClickHandler();
    } else if (currentPlayerValue.equals("amazon_music")) {
      return new AmazonMusicClickHandler();
    }
    return new DefaultClickHandler();
  }

  public void handleClick(Song song) {
    try {
      getAppropriateHandler().handlePlaySongClick(mContext, song);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "Caught some BOGUS error on the song click. Using default handler. Sad!", e);
      new DefaultClickHandler().handlePlaySongClick(mContext, song);
    }
  }
}
