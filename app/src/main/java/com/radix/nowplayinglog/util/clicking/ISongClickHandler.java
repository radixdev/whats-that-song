package com.radix.nowplayinglog.util.clicking;

import android.content.Context;

import com.radix.nowplayinglog.models.Song;

public interface ISongClickHandler {
  void handleClick(Context context, Song song);
}
