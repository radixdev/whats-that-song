package com.radix.nowplayinglog.util.clicking;

import android.content.Context;

import com.radix.nowplayinglog.models.Song;

public interface ISongClickHandler {
  void handlePlaySongClick(Context context, Song song);
}
