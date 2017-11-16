package com.radix.nowplayinglog.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;

import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

  private final List<Song> mSongData;

  public SongListAdapter(List<Song> songData) {
    mSongData = songData;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // Create a new view.
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.song_item, parent, false);

    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Song song = mSongData.get(position);
    holder.mArtistTextView.setText(song.getArtist());
    holder.mTitleTextView.setText(song.getTitle());
  }

  @Override
  public int getItemCount() {
    return mSongData.size();
  }

  /**
   * Adds a song to the current list of displayed songs
   */
  public void addSong(Song newSong) {

  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitleTextView;
    public TextView mArtistTextView;

    public ViewHolder(View v) {
      super(v);
      mTitleTextView = v.findViewById(R.id.songTitleTextView);
      mArtistTextView = v.findViewById(R.id.songArtistTextView);
    }
  }
}
