package com.radix.nowplayinglog.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.radix.nowplayinglog.models.Song;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SongStorageThing {
  private static final String TAG = SongStorageThing.class.getName();
  private static final String TITLE_KEY = "t";
  private static final String ARTIST_KEY = "a";
  private static final String POST_TIME_KEY = "ts";
  private static final String FAVORITED_KEY = "f";
  private static final String LATITUDE_KEY = "lt";
  private static final String LONGITUDE_KEY = "lg";
  private static final String SONG_STORAGE_PREFS_LOCATION = "songs.go.here";

  private static final String SONG_LAST_POSTED_PREFS_LOCATION = "songs.history";
  private static final String SONG_LAST_POSTED_ID_KEY = "songs.history.id";

  private final SharedPreferences mSongStore;
  private final SharedPreferences mSongLastPosted;

  public SongStorageThing(Context context) {
    mSongStore = context.getSharedPreferences(SONG_STORAGE_PREFS_LOCATION, Context.MODE_PRIVATE);
    mSongLastPosted = context.getSharedPreferences(SONG_LAST_POSTED_PREFS_LOCATION, Context.MODE_PRIVATE);

//    correctAllSongsInStorage();
    addTestSongs();
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

  /**
   * Stores or updates a song
   */
  public void storeSong(Song song) {
    JSONObject songObject = new JSONObject();

    try {
      songObject.put(TITLE_KEY, song.getTitle());
      songObject.put(ARTIST_KEY, song.getArtist());
      songObject.put(POST_TIME_KEY, song.getPostTime());
      songObject.put(FAVORITED_KEY, song.getIsFavorited());
      songObject.put(LATITUDE_KEY, song.getLatitude());
      songObject.put(LONGITUDE_KEY, song.getLongitude());
    } catch (JSONException e) {
      Log.e(TAG, "Failed to add song to storage: " + song, e);
    }

    SharedPreferences.Editor editor = mSongStore.edit();
    editor.putString(song.getId(), songObject.toString());
    editor.apply();

    // Update the last song metadata
    editor = mSongLastPosted.edit();
    editor.putString(SONG_LAST_POSTED_ID_KEY, song.getId());
    editor.apply();
  }

  public Song getSong(String id) {
    String songJsonBody = mSongStore.getString(id, null);

    if (songJsonBody != null) {
      return getSongFromJsonBody(id, songJsonBody);
    }
    return null;
  }

  public void deleteSong(Song song) {
    if (song == null) {
      Log.i(TAG, "Not deleting null song!");
      return;
    }
    Log.d(TAG, "Deleting song: " + song);

    SharedPreferences.Editor editor = mSongStore.edit();
    editor.remove(song.getId());
    editor.apply();

    // Update the last song metadata (if needed)
    // TODO: 11/26/2017 Replace the last song here! 
    if (mSongLastPosted.getString(SONG_LAST_POSTED_ID_KEY, "").equals(song.getId())) {
      Log.d(TAG, "Deleting the last posted song too");
      editor = mSongLastPosted.edit();
      editor.remove(SONG_LAST_POSTED_ID_KEY);
      editor.apply();
    }
  }

  /**
   * Checks if the song was posted directly previous to the last one.
   */
  public boolean isSongRepost(Song currentSong) {
    String lastPostedSongId = mSongLastPosted.getString(SONG_LAST_POSTED_ID_KEY, null);
    if (lastPostedSongId == null) {
      return false;
    }

    Song lastSong = getSong(lastPostedSongId);
    return currentSong.equals(lastSong);
  }

  public boolean isStorageEmpty() {
    return mSongStore.getAll().isEmpty();
  }

  public String getNumberOfStoredSongsForDisplay() {
    int numSongs = mSongStore.getAll().size();
    String suffix = numSongs == 1 ? "song" : "songs";
    return "(" + numSongs + " " + suffix + ")";
  }

  private static Song getSongFromJsonBody(String songId, String jsonBody) {
    try {
      JSONObject songJson = new JSONObject(jsonBody);
      return new Song(songId, songJson.getString(TITLE_KEY), songJson.getString(ARTIST_KEY),
          songJson.getLong(POST_TIME_KEY), songJson.getBoolean(FAVORITED_KEY),
          songJson.getDouble(LATITUDE_KEY), songJson.getDouble(LONGITUDE_KEY));
    } catch (JSONException e) {
      Log.e(TAG, "Failed to retrieve song from storage: " + jsonBody, e);
    }
    return null;
  }

  private void addTestSongs() {
    if (!getAllSongs().isEmpty()) {
      return;
    }

    List<String> songNames = new ArrayList<>();
    songNames.add("I Am a Rock by Simon and Garfunkel");
    songNames.add("I'm Not In Love by 10cc");
    songNames.add("New Freezer by Rich the Kid");
    songNames.add("Broke by Lecrae");
    songNames.add("Lost Boy by Jaden Smith");
    songNames.add("Bust Down by Trippie Redd");

    List<Location> locations = new ArrayList<>();
    Location location = new Location("");
    location.setLatitude(40.749153d);
    location.setLongitude(-73.996565d);
    locations.add(location);

    location = new Location("");
    location.setLatitude(40.747215d);
    location.setLongitude(-73.9981112d);
    locations.add(location);

    location = new Location("");
    location.setLatitude(40.754755d);
    location.setLongitude(-73.993230d);
    locations.add(location);

    location = new Location("");
    location.setLatitude(40.756527d);
    location.setLongitude(-73.997736d);
    locations.add(location);

    location = new Location("");
    location.setLatitude(40.746303d);
    location.setLongitude(-74.003861d);
    locations.add(location);

    final long postTime = System.currentTimeMillis() / 1000L;

    for (int i = 0; i < songNames.size(); i++) {
      Location loc = locations.get(i % locations.size());
      Song fakeSong1 = new Song(songNames.get(i), postTime + i + 100L, loc);
      storeSong(fakeSong1);
    }
  }

  /**
   * Adds any missing values for any song in storage
   */
  private void correctAllSongsInStorage() {
    final Map<String, ?> allSongs = mSongStore.getAll();
    for (Map.Entry songEntry : allSongs.entrySet()) {
      String songId = (String) songEntry.getKey();

      // Default any values that are needed
      final String songEntryValue = (String) songEntry.getValue();
      try {
        JSONObject songJson = new JSONObject(songEntryValue);

        if (!songJson.has(FAVORITED_KEY)) {
          songJson.put(FAVORITED_KEY, false);
        }
        if (!songJson.has(LATITUDE_KEY)) {
          songJson.put(LATITUDE_KEY, -1d);
        }
        if (!songJson.has(LONGITUDE_KEY)) {
          songJson.put(LONGITUDE_KEY, -1d);
        }

        SharedPreferences.Editor editor = mSongStore.edit();
        editor.putString(songId, songJson.toString());
        editor.apply();
      } catch (JSONException e) {
        Log.e(TAG, "Failed to correct song to in storage: " + songEntryValue, e);
      }

    }
  }
}
