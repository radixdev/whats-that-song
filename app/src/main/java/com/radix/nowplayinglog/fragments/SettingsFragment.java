package com.radix.nowplayinglog.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.radix.nowplayinglog.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = SettingsFragment.class.getSimpleName();
  // Identifier for the permission request
  private static final int READ_LOCATION_PERMISSIONS_REQUEST = 1;

  SharedPreferences sharedPreferences;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    //add xml
    addPreferencesFromResource(R.xml.settings_preferences);

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

    onSharedPreferenceChanged(sharedPreferences, getString(R.string.movies_categories_key));
  }


  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Preference preference = findPreference(key);
    if (preference instanceof ListPreference) {
      ListPreference listPreference = (ListPreference) preference;
      int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
      if (prefIndex >= 0) {
        preference.setSummary(listPreference.getEntries()[prefIndex]);
      }
    } else {
      preference.setSummary(sharedPreferences.getString(key, ""));
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    //unregister the preferenceChange listener
    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    //unregister the preference change listener
    getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
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
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
