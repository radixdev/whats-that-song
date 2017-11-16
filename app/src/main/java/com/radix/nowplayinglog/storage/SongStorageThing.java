package com.radix.nowplayinglog.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.radix.nowplayinglog.models.Song;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SongStorageThing {
  private static final String TAG = SongStorageThing.class.getName();
  private static final String TITLE_KEY = "title";
  private static final String ARTIST_KEY = "artist";
  private static final String POST_TIME_KEY = "timestamp";
  private static final String SONG_STORAGE_PREFS_LOCATION = "songs.go.here";

  private final SharedPreferences mSongStore;

  public SongStorageThing(Context context) {
    mSongStore = context.getSharedPreferences(SONG_STORAGE_PREFS_LOCATION, Context.MODE_PRIVATE);
  }

  public List<Song> getAllSongs() {
    final ArrayList<Song> songs = new ArrayList<>();
    final Map<String, ?> allSongs = mSongStore.getAll();
    for (Map.Entry songEntry : allSongs.entrySet()) {
      String songId = (String) songEntry.getKey();
      Song song = getSongFromJsonBody(songId, (String) songEntry.getValue());
      if (song != null) {
        songs.add(song);
      }
    }
    return songs;
  }

  public void storeSong(Song song) {
    // TODO: 11/16/2017 Check the last time the song was posted. Directly previous and within 5 min should be excluded!
    JSONObject songObject = new JSONObject();

    try {
      songObject.put(TITLE_KEY, song.getTitle());
      songObject.put(ARTIST_KEY, song.getArtist());
      songObject.put(POST_TIME_KEY, song.getPostTime());
    } catch (JSONException e) {
      Log.e(TAG, "Failed to add song to storage: " + song, e);
    }

    SharedPreferences.Editor editor = mSongStore.edit();
    editor.putString(song.getId(), songObject.toString());
    editor.apply();
  }

  public Song getSong(String id) {
    String songJsonBody = mSongStore.getString(id, null);

    if (songJsonBody != null) {
      return getSongFromJsonBody(id, songJsonBody);
    }
    return null;
  }

  private static Song getSongFromJsonBody(String songId, String jsonBody) {
    try {
      JSONObject songJson = new JSONObject(jsonBody);
      return new Song(songJson.getString(TITLE_KEY), songJson.getString(ARTIST_KEY),
          songJson.getLong(POST_TIME_KEY), songId);
    } catch (JSONException e) {
      Log.e(TAG, "Failed to retrieve song from storage: " + jsonBody, e);
    }
    return null;
  }
}
