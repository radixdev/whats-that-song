package com.radix.nowplayinglog.util;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SongUtilsTest {
  @Test
  public void testGetArtistsFromSong() {
    String fullArtist = "Migos, Nicki Minaj & Cardi B";
    List<String> allArtists = SongUtils.getArtistsFromSong(fullArtist);

    assertEquals(3, allArtists.size());
    assertEquals("Migos", allArtists.get(0));
    assertEquals("Nicki Minaj", allArtists.get(1));
    assertEquals("Cardi B", allArtists.get(2));
  }

  @Test
  public void testGetArtistsFromSongWithSimpleAmpersand() {
    String fullArtist = "N.E.R.D & Rihanna";
    List<String> allArtists = SongUtils.getArtistsFromSong(fullArtist);

    assertEquals(2, allArtists.size());
    assertEquals("N.E.R.D", allArtists.get(0));
    assertEquals("Rihanna", allArtists.get(1));
  }
}
