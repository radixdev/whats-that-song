package com.radix.nowplayinglog.util;

import com.radix.nowplayinglog.models.Song;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongSorter {

  public static void sortSongs(List<Song> songs) {
    Collections.sort(songs, new Comparator<Song>() {
      @Override
      public int compare(Song song1, Song song2) {
        if (song1.getPostTime() < song2.getPostTime()) {
          return 1;
        } else {
          return -1;
        }
      }
    });
  }
}
