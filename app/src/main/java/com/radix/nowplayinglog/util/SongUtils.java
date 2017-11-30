package com.radix.nowplayinglog.util;

import com.radix.nowplayinglog.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class SongUtils {
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

  public static void removeSongsWithoutLocationSet(List<Song> songs) {
    final Iterator<Song> songIterator = songs.iterator();
    while (songIterator.hasNext()) {
      Song song = songIterator.next();
      if (!song.hasLocationSet()) {
        songIterator.remove();
      }
    }
  }

  public static List<String> getArtistsFromSong(String fullArtist) {
    // "Migos, Nicki Minaj & Cardi B" is fucking tough
    // "A, B & C" -> "A ~ B ~ C"
    List<String> artists = new ArrayList<>();

    String customDelimiter = "||||||||||||||||||||";
    fullArtist = fullArtist.replace(",", customDelimiter);
    fullArtist = fullArtist.replace("&", customDelimiter);

    StringTokenizer tokenizer = new StringTokenizer(fullArtist, customDelimiter);

    // Now split on this string
    while (tokenizer.hasMoreTokens()) {
      final String artist = tokenizer.nextToken();
      artists.add(artist.trim());
    }

    return artists;
  }
}
