package com.radix.nowplayinglog.models;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SongTest {
  private Context mContext;

  @Before
  public void setUp() {
    mContext = InstrumentationRegistry.getContext();
    changeLanguage(Locale.US);
  }

  @Test
  public void testGetSong() {
    String body = "New Freezer by Rich the Kid";
    Song song = new Song(mContext, body, 0, null);
    Assert.assertEquals("New Freezer", song.getTitle());
    Assert.assertEquals("Rich the Kid", song.getArtist());
  }

  @Test
  public void testGetSongWithByInArtist() {
    String body = "New Freezer by Richby the Kid";
    Song song = new Song(mContext, body, 0, null);
    Assert.assertEquals("New Freezer", song.getTitle());
    Assert.assertEquals("Richby the Kid", song.getArtist());
  }

  @Test
  public void testGetSongWithByInTitle() {
    String body = "Newby Freezer by Rich the Kid";
    Song song = new Song(mContext, body, 0, null);
    Assert.assertEquals("Newby Freezer", song.getTitle());
    Assert.assertEquals("Rich the Kid", song.getArtist());
  }

  @Test
  public void testGetSongWithByAsLastString() {
    // This really just shouldn't crash lol
    String body = "New Freezer by Rich the Kidby";
    Song song = new Song(mContext, body, 0, null);
    Assert.assertEquals("New Freezer", song.getTitle());
    Assert.assertEquals("Rich the Kidby", song.getArtist());
  }

  @Test
  public void testGetSongWithSeveralDelimiters() {
    // This really just shouldn't crash lol
    String body = "ByBybyby by Bobby Brown ";
    Song song = new Song(mContext, body, 0, null);
    Assert.assertEquals("ByBybyby", song.getTitle());
    Assert.assertEquals("Bobby Brown", song.getArtist());
  }

  @Test(expected = StringIndexOutOfBoundsException.class)
  public void testGetSongThrowsExceptionOnMalformedBody() {
    String body = "Molly dolly with super bobby";
    Song song = new Song(mContext, body, 0, null);
  }

  @Test
  public void testGetSongInGermany() {
    changeLanguage(Locale.GERMANY);
    String body = "song von artist";
    Song song = new Song(mContext, body, 0, null);
    Assert.assertEquals("song", song.getTitle());
    Assert.assertEquals("artist", song.getArtist());
  }

  private void changeLanguage(Locale locale) {
    Locale.setDefault(locale);
  }
}
