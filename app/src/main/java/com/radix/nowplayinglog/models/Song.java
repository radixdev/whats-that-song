package com.radix.nowplayinglog.models;

import android.content.Context;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;

import com.radix.nowplayinglog.R;

import java.util.Calendar;
import java.util.Locale;

/**
 * The song object
 */
public class Song {
  private static final String TAG = Song.class.getName();
  private static final double LOCATION_DEFAULT_VALUE = -1d;

  private final String mTitle;
  private final String mArtist;
  private final long mPostTime;
  /**
   * A unique id for this song.
   */
  private final String mId;
  private boolean mIsFavorited;

  private final double mLatitude;
  private final double mLongitude;

  /**
   * Parses the title and artist from the notification.
   *
   * @param context
   * @param notificationTitle the title straight from the notification itself
   */
  public Song(Context context, String notificationTitle, long postTime, Location heardAtLocation) {
    // Don't want songs with "by" in the title to fuck it up
    String delimiter = getByDelimiter(context);

    if (!notificationTitle.contains(delimiter)) {
      Log.i(TAG, "notification did not contain delimiter : " + delimiter + " : notif was " + notificationTitle);
      mTitle = notificationTitle;
      mArtist = "";
    } else {
      int lastByIndex = notificationTitle.lastIndexOf(delimiter);
      String leftPart = notificationTitle.substring(0, lastByIndex).trim();
      String rightPart = notificationTitle.substring(lastByIndex + delimiter.length(), notificationTitle.length()).trim();

      if (!isRightToLeft()) {
        // english
        mTitle = leftPart;
        mArtist = rightPart;
      } else {
        // korean for example
        mTitle = rightPart;
        mArtist = leftPart;
      }
    }

    mPostTime = postTime;
    mId = String.valueOf(mPostTime);
    mIsFavorited = false;

    if (heardAtLocation != null) {
      mLatitude = heardAtLocation.getLatitude();
      mLongitude = heardAtLocation.getLongitude();
    } else {
      mLatitude = LOCATION_DEFAULT_VALUE;
      mLongitude = LOCATION_DEFAULT_VALUE;
    }
  }

  /**
   * Used to recreate the Song from storage.
   */
  public Song(String id, String title, String artist, long postTime, boolean isFavorited, double latitude, double longitude) {
    mTitle = title;
    mArtist = artist;
    mPostTime = postTime;
    mId = id;
    mIsFavorited = isFavorited;

    mLatitude = latitude;
    mLongitude = longitude;
  }

  /**
   * Creates a new song but with location fields set
   */
  public Song(Song existingSong, Location location) {
    mTitle = existingSong.mTitle;
    mArtist = existingSong.mArtist;
    mPostTime = existingSong.mPostTime;
    mId = existingSong.mId;
    mIsFavorited = existingSong.mIsFavorited;

    mLatitude = location.getLatitude();
    mLongitude = location.getLongitude();
  }

  public String getTitleAndArtistForDisplay() {
    return mTitle + " by " + mArtist;
  }

  public String getLatLngForDisplay() {
    return "(" + mLatitude + ", " + mLongitude + ")";
  }

  public String getId() {
    return mId;
  }

  public String getTitle() {
    return mTitle;
  }

  public String getArtist() {
    return mArtist;
  }

  public long getPostTime() {
    return mPostTime;
  }

  public boolean getIsFavorited() {
    return mIsFavorited;
  }

  public void setFavorited(boolean favorited) {
    mIsFavorited = favorited;
  }

  public double getLatitude() {
    return mLatitude;
  }

  public double getLongitude() {
    return mLongitude;
  }

  public boolean hasLocationSet() {
    return mLatitude != LOCATION_DEFAULT_VALUE && mLongitude != LOCATION_DEFAULT_VALUE;
  }

  public String getPrettyDate() {
    Calendar cal = Calendar.getInstance(Locale.US);
    cal.setTimeInMillis(mPostTime);
    return DateFormat.format("MMMM dd hh:mm aa", cal).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Song song = (Song) o;

    if (!mTitle.equals(song.mTitle)) return false;
    return mArtist.equals(song.mArtist);
  }

  @Override
  public int hashCode() {
    int result = mTitle.hashCode();
    result = 31 * result + mArtist.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Song{" +
        "mTitle='" + mTitle + '\'' +
        ", mArtist='" + mArtist + '\'' +
        ", mPostTime=" + mPostTime +
        ", mId='" + mId + '\'' +
        ", mIsFavorited=" + mIsFavorited +
        ", mLatitude=" + mLatitude +
        ", mLongitude=" + mLongitude +
        '}';
  }

  private String getByDelimiter(Context context) {
    return context.getString(R.string.delimiter);
  }

  private boolean isRightToLeft() {
    String defaultLanguage = Locale.getDefault().getLanguage();

    if (defaultLanguage.equals(Locale.KOREAN.getLanguage())) {
      return true;
    }
    return false;
  }
}
