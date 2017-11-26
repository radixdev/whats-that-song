package com.radix.nowplayinglog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.art.AlbumArtDownloaderAsyncTask;
import com.radix.nowplayinglog.fragments.swiping.ItemTouchHelperAdapter;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.SongSorter;
import com.radix.nowplayinglog.util.clicking.ClickHandlerProvider;

import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements ItemTouchHelperAdapter {
  private static final String TAG = SongStorageThing.class.getName();

  private final List<Song> mSongData;
  private final ClickHandlerProvider mClickHandlerProvider;
  private final Context mContext;
  private final SongStorageThing mSongStorage;
  private final SongListFragment.OnSongMapIconPressedListener mSongMapClickCallback;

  SongListAdapter(Context context, List<Song> songData,
                         SongStorageThing songStorage, SongListFragment.OnSongMapIconPressedListener songClickCallback) {
    mContext = context.getApplicationContext();
    mSongData = songData;
    mSongStorage = songStorage;
    mClickHandlerProvider = new ClickHandlerProvider(mContext);
    mSongMapClickCallback = songClickCallback;

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
    holder.mSongDateTextView.setText(song.getPrettyDate());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mClickHandlerProvider.handleClick(song);
      }
    });

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(holder.itemView.getContext());
        alert.setTitle("Alert!!");
        alert.setMessage("Are you sure to delete record");
        alert.setItems(R.array.song_long_press_options, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                Log.d(TAG, "User pressed share in dialog");
                break;
              case 1:
                Log.d(TAG, "User pressed map in dialog");
                break;
              case 2:
                Log.d(TAG, "User pressed favorite in dialog");
                break;
              case 3:
                Log.d(TAG, "User pressed delete in dialog");
                break;
            }

            dialog.dismiss();
          }
        });

        alert.show();
        return true;
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
          mSongMapClickCallback.onSongMapClicked(song);
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

  @Override
  public void onItemDismiss(int position) {
    Song removedSong = mSongData.remove(position);
    notifyItemRemoved(position);

    // Delete from storage
    mSongStorage.deleteSong(removedSong);

    Intent removedSongIntent = new Intent(Constants.REMOVED_SONG_BROADCAST_FILTER);
    removedSongIntent.putExtra(Constants.BROADCAST_FILTER_SONG_ID, removedSong.getId());
    LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(removedSongIntent);
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
    TextView mSongDateTextView;
    ImageView mAlbumArtImage;
    AlbumArtDownloaderAsyncTask mImageLoaderTask;

    ImageButton mFavoriteButton;
    ImageButton mMapIconButton;

    ViewHolder(View v) {
      super(v);
      mTitleTextView = v.findViewById(R.id.songTitleTextView);
      mArtistTextView = v.findViewById(R.id.songArtistTextView);
      mSongDateTextView = v.findViewById(R.id.songPostTimeTextView);
      mAlbumArtImage = v.findViewById(R.id.songAlbumImage);
      mFavoriteButton = v.findViewById(R.id.imageButtonFavorite);
      mMapIconButton = v.findViewById(R.id.imageButtonMap);
    }
  }
}
