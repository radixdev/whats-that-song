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
        openSongInPlayer(song);
      }
    });

    final ImageButton favoriteButton = holder.mFavoriteButton;
    favoriteButton.setSelected(song.getIsFavorited());
    favoriteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        favoriteSong(favoriteButton, song);
      }
    });

    // Hide the map icon if needed
    if (song.hasLocationSet()) {
      holder.mMapIconButton.setVisibility(View.VISIBLE);
      holder.mMapIconButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openSongInMap(song);
        }
      });
    } else {
      holder.mMapIconButton.setVisibility(View.INVISIBLE);
    }

    if (holder.mSong == null || !holder.mSong.equals(song)) {
      // Need to redraw the song
      if (holder.mAlbumArtImage != null) {
        holder.mAlbumArtImage.setImageBitmap(null);
      }

      if (holder.mImageLoaderTask != null) {
        holder.mImageLoaderTask.cancel(true);
      }

      AlbumArtDownloaderAsyncTask task = new AlbumArtDownloaderAsyncTask(holder.mAlbumArtImage, song, mContext);
      holder.mImageLoaderTask = task;
      task.execute();
    }

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(holder.itemView.getContext());
        alert.setTitle(song.getTitleAndArtistForDisplay());
        final int song_long_press_options;
        if (song.hasLocationSet()) {
          song_long_press_options = R.array.song_long_press_options;
        } else {
          song_long_press_options = R.array.song_long_press_options_without_map;
        }

        alert.setItems(song_long_press_options, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String option = mContext.getResources().getStringArray(song_long_press_options)[which];
            Log.d(TAG, "User pressed " + option + " in dialog");
            switch (option) {
              case "Share":
                shareSong(song);
                break;
              case "Play Song":
                openSongInPlayer(song);
                break;
              case "Favorite":
                favoriteSong(favoriteButton, song);
                break;
              case "Find in Map":
                openSongInMap(song);
                break;
              case "Delete":
                deleteSong(holder.getAdapterPosition());
                break;
            }

            dialog.dismiss();
          }
        });

        alert.show();
        return true;
      }
    });

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
    deleteSong(position);
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

  private void openSongInPlayer(Song song) {
    mClickHandlerProvider.handleClick(song);
  }

  private void shareSong(Song song) {
    String text = song.getTitleAndArtistForDisplay() + " (" + song.getPrettyDate() + ")";
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
    sendIntent.setType("text/plain");
    mContext.startActivity(sendIntent);
  }

  private void favoriteSong(ImageButton favoriteButton, Song song) {
    song.setFavorited(!song.getIsFavorited());
    favoriteButton.setSelected(song.getIsFavorited());
    mSongStorage.storeSong(song);
  }

  private void openSongInMap(Song song) {
    mSongMapClickCallback.onSongMapClicked(song);
  }

  private void deleteSong(int adapterPosition) {
    Song removedSong = mSongData.remove(adapterPosition);
    notifyItemRemoved(adapterPosition);

    // Delete from storage
    mSongStorage.deleteSong(removedSong);

    Intent removedSongIntent = new Intent(Constants.REMOVED_SONG_BROADCAST_FILTER);
    removedSongIntent.putExtra(Constants.BROADCAST_FILTER_SONG_ID, removedSong.getId());
    LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(removedSongIntent);
  }
}
