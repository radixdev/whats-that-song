package com.radix.nowplayinglog.util.clicking;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import com.radix.nowplayinglog.models.Song;

/**
 *
 */
public class AmazonMusicClickHandler implements ISongClickHandler {
  private static final String TAG = AmazonMusicClickHandler.class.getName();

  @Override
  public void handlePlaySongClick(Context context, Song song) {
    Log.v(TAG, "Handling click for " + song);

    Intent intent = new Intent();
    intent.putExtra(SearchManager.QUERY, song.getTitle());
    intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, song.getArtist());
    intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song.getTitle());

    // Amazon only shit
    // https://github.com/luismanuel001/TopCharts/blob/8463a8b033e5fdbb3dabd28ea75c3a4ccc3c2730/Top%20Charts/src/android/topcharts/SongInfoActivity.java#L93
    intent.setAction("com.amazon.mp3.action.EXTERNAL_EVENT");
    intent.putExtra("com.amazon.mp3.extra.EXTERNAL_EVENT_TYPE","com.amazon.mp3.type.SEARCH");
    intent.putExtra("com.amazon.mp3.extra.SEARCH_TYPE", 0); // 0 = Song, 1 = album
    intent.putExtra("com.amazon.mp3.extra.SEARCH_STRING", song.getTitle() + " " + song.getArtist());

    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    context.startActivity(intent);
  }
}
