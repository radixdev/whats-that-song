package com.radix.nowplayinglog.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.art.AlbumArtDownloaderAsyncTask;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.util.clicking.ClickHandlerProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

  private final List<Song> mSongData;
  private final ClickHandlerProvider mClickHandlerProvider;
  private final Context mContext;

  public SongListAdapter(Context context, List<Song> songData) {
    mContext = context.getApplicationContext();
    mSongData = songData;
    mClickHandlerProvider = new ClickHandlerProvider();

    Collections.sort(mSongData, new Comparator<Song>() {
      @Override
      public int compare(Song song1, Song song2) {
        if (song1.getPostTime() < song2.getPostTime()) {
          return 1;
        } else {
          return -1;
        }
      }
    });

    setHasStableIds(true);
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
    final Song song = mSongData.get(position);
    holder.mArtistTextView.setText(song.getArtist());
    holder.mTitleTextView.setText(song.getTitle());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mClickHandlerProvider.getAppropriateHandler().handleClick(mContext, song);
      }
    });
    if (holder.mImageLoaderTask != null) {
      holder.mImageLoaderTask.cancel(true);
    }

    Glide.with(mContext)
        .load("https://ih1.redbubble.net/image.63252269.3457/flat,800x800,075,t.u2.jpg")
        .into(holder.mAlbumArtImage);

    AlbumArtDownloaderAsyncTask task = new AlbumArtDownloaderAsyncTask(holder.mAlbumArtImage, song, mContext);
    holder.mImageLoaderTask = task;
    task.execute();
  }

  @Override
  public int getItemCount() {
    return mSongData.size();
  }

  @Override
  public long getItemId(int position) {
    return mSongData.get(position).getId().hashCode();
  }

  /**
   * Adds a song to the current list of displayed songs
   */
  void addSong(Song newSong) {
    mSongData.add(0, newSong);
    notifyItemChanged(0);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView mTitleTextView;
    TextView mArtistTextView;
    ImageView mAlbumArtImage;
    AlbumArtDownloaderAsyncTask mImageLoaderTask;

    ViewHolder(View v) {
      super(v);
      mTitleTextView = v.findViewById(R.id.songTitleTextView);
      mArtistTextView = v.findViewById(R.id.songArtistTextView);
      mAlbumArtImage = v.findViewById(R.id.songAlbumImage);
    }
  }
}
