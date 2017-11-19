package com.radix.nowplayinglog.models;

import android.location.Location;

/**
 * The song object
 */
public class Song {
  private static final String BY_DELIMITER = "by";
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
   * @param notificationTitle the title straight from the notification itself
   * @param heardAtLocation
   */
  public Song(String notificationTitle, long postTime, Location heardAtLocation){
    // Don't want songs with "by" in the title to fuck it up
    int lastByIndex = notificationTitle.lastIndexOf(BY_DELIMITER);
    mTitle = notificationTitle.substring(0, lastByIndex).trim();
    mArtist = notificationTitle.substring(lastByIndex + BY_DELIMITER.length(), notificationTitle.length()).trim();

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
}
