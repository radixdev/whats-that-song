package com.radix.nowplayinglog.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.PermissionUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = SettingsFragment.class.getSimpleName();

  public interface OnSettingsButtonClickedListener {
    void onGoogleDriveBackupClicked();
  }

  private OnSettingsButtonClickedListener mSettingsButtonClickedCallback;
  private SharedPreferences mSettingsPrefs;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_preferences);
    mSettingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    onSharedPreferenceChanged(mSettingsPrefs, getString(R.string.settings_map_key));
    onSharedPreferenceChanged(mSettingsPrefs, getString(R.string.settings_music_player_key));
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Preference preference = findPreference(key);

    if (preference instanceof CheckBoxPreference) {
      CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
      if (key.equals(getString(R.string.settings_map_key))) {
        // Ask for the permission if needed
        if (checkBoxPreference.isChecked()) {
          getPermissionToReadLocation();
        }
      }
    } else if (preference instanceof ListPreference) {
      ListPreference listPreference = (ListPreference) preference;
      if (key.equals(getString(R.string.settings_music_player_key))) {
        String value = (String) listPreference.getEntry();
        preference.setSummary("Music player set to: " + value);
      }
    } else {
      // It's just a button...
      if (key.equals(getString(R.string.settings_google_drive_backup_key))) {
        mSettingsButtonClickedCallback.onGoogleDriveBackupClicked();
      }
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

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mSettingsButtonClickedCallback = (OnSettingsButtonClickedListener) getActivity();
    } catch (ClassCastException e) {
      throw new ClassCastException(getActivity().toString()
          + " must implement OnSettingsButtonClickedListener");
    }
  }

  public void getPermissionToReadLocation() {
    if (!PermissionUtils.isPermissionGranted(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
      // Fire off an async request to actually get the permission
      requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          Constants.READ_LOCATION_PERMISSIONS_REQUEST);
    }
  }
}
