package com.radix.nowplayinglog.util.clicking;

import android.content.Context;
import android.util.Log;

import com.radix.nowplayinglog.models.Song;

/**
 *
 */
public class DefaultClickHandler implements ISongClickHandler {
  private static final String TAG = DefaultClickHandler.class.getName();

  @Override
  public void handleClick(Context context, Song song) {
    Log.v(TAG, "Handling click for " + song);

//    Intent intent = new Intent();
//    intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
//    intent.putExtra(SearchManager.QUERY, song.getTitle());
//    intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, song.getArtist());
//    intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song.getTitle());
//
//    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    context.startActivity(intent);
  }
}
