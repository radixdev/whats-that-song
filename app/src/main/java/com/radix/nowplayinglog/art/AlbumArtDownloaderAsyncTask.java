package com.radix.nowplayinglog.art;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

// Fake news
@SuppressLint("StaticFieldLeak")
public class AlbumArtDownloaderAsyncTask extends AsyncTask<Void, Void, String> {
  private static final String TAG = AlbumArtDownloaderAsyncTask.class.getName();

  private final Context mContext;
  private final ImageView mImageView;
  private final Song mSong;

  private static final Map<String, Integer> LAST_FM_IMAGE_SIZES = new HashMap<>();

  static {
    LAST_FM_IMAGE_SIZES.put("small", 1);
    LAST_FM_IMAGE_SIZES.put("medium", 2);
    LAST_FM_IMAGE_SIZES.put("large", 3);
    LAST_FM_IMAGE_SIZES.put("extralarge", 4);
  }

  public AlbumArtDownloaderAsyncTask(ImageView imageView, Song song, Context context) {
    mImageView = imageView;
    mSong = song;
    mContext = context;
  }

  @Override
  protected String doInBackground(Void... voids) {
    // Spool up some okhttp and parse the album art out of it
    Log.d(TAG, "Downloading art for song: " + mSong);

    String imageUrl;

    // Try for the track art, then the artist art

    final String lastFmUrlForSong = getLastFmUrlForSong(mSong, true);
    Log.d(TAG, "Using url " + lastFmUrlForSong);
    Request request = new Request.Builder()
        .url(lastFmUrlForSong).build();

    JSONObject json = getJsonDataFromRequest(request);
    if (json == null) {
      return null;
    }

    try {
      Log.d(TAG, "Using json: " + json.toString(4));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    // Now let's parse
    imageUrl = retrieveUrlFromLastFmTrackResponse(json);

    if (imageUrl != null) {
      return imageUrl;
    }

    Log.d(TAG, "Could not find track image, using artist image instead");

    // Attempt for the artist image then
    final String lastFmUrlForArtist = getLastFmUrlForSong(mSong, false);
    Log.d(TAG, "Using artist url " + lastFmUrlForSong);
    request = new Request.Builder()
        .url(lastFmUrlForArtist).build();

    json = getJsonDataFromRequest(request);
    if (json == null) {
      return null;
    }

    try {
      Log.d(TAG, "Using json: " + json.toString(4));
    } catch (JSONException e) {
      e.printStackTrace();
    }

    // Now let's parse for the artist pic
    imageUrl = retrieveUrlFromLastFmArtistResponse(json);

    return imageUrl;
  }

  @Override
  protected void onPostExecute(String imageUrl) {
    // Make sure we don't overwrite any images
    Glide.with(mContext)
        .load(imageUrl)
        .into(mImageView);
  }

  private static JSONObject getJsonDataFromRequest(Request request) {
    OkHttpClient client = new OkHttpClient();

    try {
      Response response = client.newCall(request).execute();
      final ResponseBody body = response.body();
      if (body == null) {
        return null;
      }
      String jsonData = body.string();
      return new JSONObject(jsonData);
    } catch (Exception e) {
      Log.e(TAG, "Fucked up while downloading album art from last fm", e);
    }

    return null;
  }

  private static String retrieveUrlFromLastFmTrackResponse(JSONObject json) {
    try {
      JSONArray imageObjects = json.getJSONObject("track").getJSONObject("album").getJSONArray("image");

      // We want the largest, but who knows if it's available!
      String bestImageUrl = null;
      int bestSize = 0;
      for (int i = 0; i < imageObjects.length(); i++) {
        JSONObject jsonObject = imageObjects.getJSONObject(i);
        String size = jsonObject.getString("size");

        int sizeScore = LAST_FM_IMAGE_SIZES.get(size);
        if (sizeScore > bestSize) {
          bestImageUrl = jsonObject.getString("#text");
          bestSize = sizeScore;
        }
      }

      Log.d(TAG, "Found image url for song " + bestImageUrl);
      return bestImageUrl;
    } catch (Exception e) {
      Log.e(TAG, "Failed to retrieve url from response with json", e);
      return null;
    }
  }

  private static String retrieveUrlFromLastFmArtistResponse(JSONObject json) {
    try {
      JSONArray imageObjects = json.getJSONObject("track").getJSONObject("album").getJSONArray("image");

      // We want the largest, but who knows if it's available!
      String bestImageUrl = null;
      int bestSize = 0;
      for (int i = 0; i < imageObjects.length(); i++) {
        JSONObject jsonObject = imageObjects.getJSONObject(i);
        String size = jsonObject.getString("size");

        int sizeScore = LAST_FM_IMAGE_SIZES.get(size);
        if (sizeScore > bestSize) {
          bestImageUrl = jsonObject.getString("#text");
          bestSize = sizeScore;
        }
      }

      Log.d(TAG, "Found image url for song " + bestImageUrl);
      return bestImageUrl;
    } catch (Exception e) {
      Log.e(TAG, "Failed to retrieve url from response with json", e);
      return null;
    }
  }

  private static String getLastFmUrlForSong(Song song, boolean isTrackSearch) {
    // http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=API_KEY&artist=cher&track=believe&format=json
    // or
    // http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=Cher&api_key=YOUR_API_KEY&format=json

    if (isTrackSearch) {
      return "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json" +
          "&api_key=" + Constants.LAST_FM_API_KEY +
          "&artist=" + song.getArtist() +
          "&track=" + song.getTitle();
    } else {
      return "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&format=json" +
          "&api_key=" + Constants.LAST_FM_API_KEY +
          "&artist=" + song.getArtist();
    }
  }
}
