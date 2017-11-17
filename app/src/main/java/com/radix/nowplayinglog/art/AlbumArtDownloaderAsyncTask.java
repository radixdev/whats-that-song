package com.radix.nowplayinglog.art;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
  private final SharedPreferences mImageUrlCache;

  private static final Map<String, Integer> LAST_FM_IMAGE_SIZES = new HashMap<>();

  static {
    int sizeRanking = 1;
    LAST_FM_IMAGE_SIZES.put("",  ++sizeRanking);
    LAST_FM_IMAGE_SIZES.put("small", ++sizeRanking);
    LAST_FM_IMAGE_SIZES.put("medium", ++sizeRanking);
    LAST_FM_IMAGE_SIZES.put("mega", ++sizeRanking);
    LAST_FM_IMAGE_SIZES.put("extralarge", ++sizeRanking);
    LAST_FM_IMAGE_SIZES.put("large", ++sizeRanking);
  }

  public AlbumArtDownloaderAsyncTask(ImageView imageView, Song song, Context context) {
    mImageView = imageView;
    mSong = song;
    mContext = context;
    mImageUrlCache = context.getSharedPreferences("lastfm.url.cache", Context.MODE_PRIVATE);
  }

  @Override
  protected String doInBackground(Void... voids) {
    // Spool up some okhttp and parse the album art out of it
    Log.d(TAG, "Downloading art for song: " + mSong);

    String imageUrl = getImageUrlFromCache(mSong);
    if (imageUrl != null) {
      // wow that was easy
      Log.d(TAG, "Retrieved url from cache: " + imageUrl);
      return imageUrl;
    }

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
      addImageUrlToCache(mSong, imageUrl);
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
    addImageUrlToCache(mSong, imageUrl);
    return imageUrl;
  }

  @Override
  protected void onPostExecute(String imageUrl) {
    if (imageUrl != null && !imageUrl.equals("")) {
      // Make sure we don't overwrite any images
      Glide.with(mContext)
          .load(imageUrl)
          .into(mImageView);
    }
  }

  private void addImageUrlToCache(Song song, String url) {
    SharedPreferences.Editor edit = mImageUrlCache.edit();
    edit.putString(song.getId(), url);
    edit.apply();
  }

  private String getImageUrlFromCache(Song song) {
    if (mImageUrlCache.contains(song.getId())) {
      return mImageUrlCache.getString(song.getId(), null);
    }

    return null;
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

      String bestImageUrl = getBestImageUrlFromImagesArray(imageObjects);

      Log.d(TAG, "Found image url for song " + bestImageUrl);
      return bestImageUrl;
    } catch (Exception e) {
      Log.e(TAG, "Failed to retrieve url from response with json", e);
      return null;
    }
  }

  private static String retrieveUrlFromLastFmArtistResponse(JSONObject json) {
    try {
      JSONArray imageObjects = json.getJSONObject("artist").getJSONArray("image");

      String bestImageUrl = getBestImageUrlFromImagesArray(imageObjects);

      Log.d(TAG, "Found image url for artist " + bestImageUrl);
      return bestImageUrl;
    } catch (Exception e) {
      Log.e(TAG, "Failed to retrieve url from response with json", e);
      return null;
    }
  }

  private static String getBestImageUrlFromImagesArray(JSONArray imageObjects) throws JSONException {
    // We want the largest, but who knows if it's available!
    String bestImageUrl = null;
    int bestSize = 0;
    for (int i = 0; i < imageObjects.length(); i++) {
      JSONObject jsonObject = imageObjects.getJSONObject(i);
      String size = jsonObject.getString("size");

      int sizeScore = LAST_FM_IMAGE_SIZES.get(size);
      final String imageUrl = jsonObject.getString("#text");
      if (sizeScore > bestSize && !imageUrl.trim().equals("")) {
        bestImageUrl = imageUrl;
        bestSize = sizeScore;
        Log.d(TAG, "found best size of " + size);
      }
    }

    return bestImageUrl;
  }

  private static String getLastFmUrlForSong(Song song, boolean isTrackSearch) {
    // http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=API_KEY&artist=cher&track=believe&format=json
    // or
    // http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=Cher&api_key=YOUR_API_KEY&format=json

    // "Migos, Nicki Minaj & Cardi B" is fucking tough
    String artist = song.getArtist();
    int lastComma = artist.lastIndexOf(",");
    int lastAmpersand = artist.lastIndexOf("&");

    final int minBadChar = Math.min(lastComma, lastAmpersand);
    String urlSafeArtist;

    if (minBadChar == -1) {
      urlSafeArtist = artist;
    } else {
      urlSafeArtist = artist.substring(0, minBadChar);
    }
    String urlSafeTitle = song.getTitle();
    if (isTrackSearch) {
      return "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json" +
          "&api_key=" + Constants.LAST_FM_API_KEY +
          "&track=" + urlSafeTitle +
          "&artist=" + urlSafeArtist;
    } else {
      return "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&format=json" +
          "&api_key=" + Constants.LAST_FM_API_KEY +
          "&artist=" + urlSafeArtist;
    }
  }
}
