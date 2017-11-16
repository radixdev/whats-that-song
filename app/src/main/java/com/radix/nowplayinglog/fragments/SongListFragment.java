package com.radix.nowplayinglog.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;

public class SongListFragment extends Fragment {
  private RecyclerView mRecyclerView;
  private SongStorageThing mSongStorageThing;
  private SongListAdapter mAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mSongStorageThing = new SongStorageThing(getContext());

    LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (mAdapter != null) {
          // Get the song id
          String id = intent.getStringExtra(Constants.NEW_SONG_BROADCAST_FILTER_SONG_ID);
          Song song = mSongStorageThing.getSong(id);
          mAdapter.addSong(song);
        }
      }
    }, new IntentFilter(Constants.NEW_SONG_BROADCAST_FILTER));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_song_list, container, false);
    mRecyclerView = rootView.findViewById(R.id.songListRecycler);
    setRecyclerViewLayoutManager();

    mAdapter = new SongListAdapter(getContext(), mSongStorageThing.getAllSongs());
    mRecyclerView.setAdapter(mAdapter);

    return rootView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  private void setRecyclerViewLayoutManager() {
    int scrollPosition = 0;

    // If a layout manager has already been set, get current scroll position.
    if (mRecyclerView.getLayoutManager() != null) {
      scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
          .findFirstCompletelyVisibleItemPosition();
    }

    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mRecyclerView.scrollToPosition(scrollPosition);
  }
}
