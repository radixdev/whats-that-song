package com.radix.nowplayinglog.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.art.AlbumArtDownloaderAsyncTask;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.SongSorter;
import com.radix.nowplayinglog.util.clicking.ClickHandlerProvider;

import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
  private final List<Song> mSongData;
  private final ClickHandlerProvider mClickHandlerProvider;
  private final Context mContext;
  private final SongStorageThing mSongStorage;
  private final SongListFragment.OnSongMapIconPressedListener mSongClickCallback;

  public SongListAdapter(Context context, List<Song> songData,
                         SongStorageThing songStorage, SongListFragment.OnSongMapIconPressedListener songClickCallback) {
    mContext = context.getApplicationContext();
    mSongData = songData;
    mSongStorage = songStorage;
    mClickHandlerProvider = new ClickHandlerProvider();
    mSongClickCallback = songClickCallback;

    SongSorter.sortSongs(mSongData);

    setHasStableIds(true);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.song_item, parent, false);

    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final Song song = mSongData.get(position);
    holder.mArtistTextView.setText(song.getArtist());
    holder.mTitleTextView.setText(song.getTitle());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mClickHandlerProvider.getAppropriateHandler().handleClick(mContext, song);
      }
    });

    final ImageButton favoriteButton = holder.mFavoriteButton;
    favoriteButton.setSelected(song.getIsFavorited());
    favoriteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        song.setFavorited(!song.getIsFavorited());
        favoriteButton.setSelected(song.getIsFavorited());
        mSongStorage.storeSong(song);
      }
    });

    // Hide the map icon if needed
    if (song.hasLocationSet()) {
      holder.mMapIconButton.setVisibility(View.VISIBLE);
      holder.mMapIconButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mSongClickCallback.onSongMapClicked(song);
        }
      });
    } else {
      holder.mMapIconButton.setVisibility(View.GONE);
    }


    if (holder.mSong == null || !holder.mSong.equals(song)) {
      // Need to redraw the song
      if (holder.mAlbumArtImage != null) {
        holder.mAlbumArtImage.setImageBitmap(null);
      }

      if (holder.mImageLoaderTask != null) {
        holder.mImageLoaderTask.cancel(true);
      }

//    Glide.with(mContext)
//        .load("https://ih1.redbubble.net/image.63252269.3457/flat,800x800,075,t.u2.jpg")
//        .into(holder.mAlbumArtImage);

      AlbumArtDownloaderAsyncTask task = new AlbumArtDownloaderAsyncTask(holder.mAlbumArtImage, song, mContext);
      holder.mImageLoaderTask = task;
      task.execute();
    }

    holder.mSong = song;
  }

  @Override
  public void onViewRecycled(ViewHolder holder) {
    super.onViewRecycled(holder);

    if (holder.mImageLoaderTask != null) {
      holder.mImageLoaderTask.cancel(true);

    }
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
    Song mSong;
    TextView mTitleTextView;
    TextView mArtistTextView;
    ImageView mAlbumArtImage;
    AlbumArtDownloaderAsyncTask mImageLoaderTask;

    ImageButton mFavoriteButton;
    ImageButton mMapIconButton;

    ViewHolder(View v) {
      super(v);
      mTitleTextView = v.findViewById(R.id.songTitleTextView);
      mArtistTextView = v.findViewById(R.id.songArtistTextView);
      mAlbumArtImage = v.findViewById(R.id.songAlbumImage);
      mFavoriteButton = v.findViewById(R.id.imageButtonFavorite);
      mMapIconButton = v.findViewById(R.id.imageButtonMap);
    }
  }
}
