package com.radix.nowplayinglog.util.clicking;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import com.radix.nowplayinglog.models.Song;

public class DeezerClickHandler implements ISongClickHandler {
  private static final String TAG = DeezerClickHandler.class.getName();

  @Override
  public void handlePlaySongClick(Context context, Song song) {
    Log.v(TAG, "Handling click for " + song);
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
    intent.setComponent(new ComponentName(
        "deezer.android.app",
        "com.deezer.android.ui.activity.LauncherActivity"));
    intent.putExtra(SearchManager.QUERY, song.getTitle());
    intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, song.getArtist());
    intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song.getTitle());

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }
}
