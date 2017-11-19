package com.radix.nowplayinglog.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.SongSorter;

import java.util.List;

public class SongMapFragment extends Fragment implements OnMapReadyCallback {
  private static final String TAG = SongMapFragment.class.getName();

  private GoogleMap mMap;
  private SongStorageThing mSongStorageThing;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSongStorageThing = new SongStorageThing(getContext());

    LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        // Get the song id
        String id = intent.getStringExtra(Constants.NEW_SONG_BROADCAST_FILTER_SONG_ID);
        Song song = mSongStorageThing.getSong(id);
        if (song != null && song.hasLocationSet() && mMap != null) {
          addSongToMap(song);
        }
      }
    }, new IntentFilter(Constants.NEW_SONG_BROADCAST_FILTER));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View rootView = inflater.inflate(R.layout.fragment_map, container, false);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_google_map);
    mapFragment.getMapAsync(this);

    return rootView;
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    try {
      // Customise the styling of the base map using a JSON object defined
      // in a raw resource file.
      boolean success = mMap.setMapStyle(
          MapStyleOptions.loadRawResourceStyle(
              getContext(), R.raw.map_style_json));

      if (!success) {
        Log.e(TAG, "Style parsing failed.");
      }
    } catch (Resources.NotFoundException e) {
      Log.e(TAG, "Can't find style. Error: ", e);
    }

    // Add all the known songs to the map
    List<Song> allSongs = mSongStorageThing.getAllSongs();
    if (allSongs.isEmpty()) {
      Log.i(TAG, "No songs available, returning");
      return;
    }
    SongSorter.sortSongs(allSongs);

    // Add all the markers
    for (Song song : allSongs) {
      addSongToMap(song);
    }

    // Move the camera to the first song in the list
    Song firstSong = allSongs.get(0);
    LatLng firstSongPos = new LatLng(firstSong.getLatitude(), firstSong.getLongitude());
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstSongPos, 15));
  }

  private void addSongToMap(Song song) {
    if (!song.hasLocationSet()) {
      return;
    }

    LatLng songPosition = new LatLng(song.getLatitude(), song.getLongitude());
    mMap.addMarker(new MarkerOptions()
        .position(songPosition)
        .title(song.getTitleAndArtistForDisplay())
    );
  }
}
