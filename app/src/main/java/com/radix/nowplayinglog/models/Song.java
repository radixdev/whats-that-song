package com.radix.nowplayinglog.models;

/**
 * The song object
 */
public class Song {
  private static final String BY_DELIMITER = "by";


  private final String mTitle;
  private final String mArtist;
  private final long mPostTime;
  /**
   * A unique id for this song.
   */
  private final String mId;

  /**
   * Parses the title and artist from the notification
   *
   * @param notificationTitle the title straight from the notification itself
   */
  public Song(String notificationTitle, long postTime){
    // Don't want songs with "by" in the title to fuck it up
    int lastByIndex = notificationTitle.lastIndexOf(BY_DELIMITER);
    mTitle = notificationTitle.substring(0, lastByIndex).trim();
    mArtist = notificationTitle.substring(lastByIndex + BY_DELIMITER.length(), notificationTitle.length()).trim();

    mPostTime = postTime;

    mId = String.valueOf(mPostTime);
//    mId = String.valueOf(notificationTitle.hashCode());
  }

  public Song(String title, String artist, long postTime, String id) {
    mTitle = title;
    mArtist = artist;
    mPostTime = postTime;
    mId = id;
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

  @Override
  public String toString() {
    return "Song{" +
        "title='" + mTitle + '\'' +
        ", artist='" + mArtist + '\'' +
        ", postTime=" + mPostTime +
        ", id='" + mId + '\'' +
        '}';
  }
}
