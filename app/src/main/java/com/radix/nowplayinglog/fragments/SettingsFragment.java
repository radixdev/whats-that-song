package com.radix.nowplayinglog.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.radix.nowplayinglog.R;

public class SettingsFragment extends Fragment {

  // Identifier for the permission request
  private static final int READ_LOCATION_PERMISSIONS_REQUEST = 1;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getPermissionToReadLocation();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_settings, container, false);
  }

  public void getPermissionToReadLocation() {
    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      // The permission is NOT already granted.

      // Fire off an async request to actually get the permission
      requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          READ_LOCATION_PERMISSIONS_REQUEST);
    }
  }

  // Callback with the request from calling requestPermissions(...)
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String permissions[],
                                         @NonNull int[] grantResults) {
    // Make sure it's our original READ_CONTACTS request
    if (requestCode == READ_LOCATION_PERMISSIONS_REQUEST) {
      if (grantResults.length == 1 &&
          grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(getContext(), "Read Location permission granted", Toast.LENGTH_SHORT).show();
      } else {
        // showRationale = false if user clicks Never Ask Again, otherwise true
        boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

        if (showRationale) {
          // do something here to handle degraded mode
        } else {
          Toast.makeText(getContext(), "Read Location permission denied", Toast.LENGTH_SHORT).show();
        }
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }
}
