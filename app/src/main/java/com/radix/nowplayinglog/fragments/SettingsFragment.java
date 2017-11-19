package com.radix.nowplayinglog.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.util.Constants;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = SettingsFragment.class.getSimpleName();

  SharedPreferences mSettingsPrefs;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    //add xml
    addPreferencesFromResource(R.xml.settings_preferences);
    mSettingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    onSharedPreferenceChanged(mSettingsPrefs, getString(R.string.settings_map_key));
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
    }
//    if (preference instanceof ListPreference) {
//      ListPreference listPreference = (ListPreference) preference;
//      int prefIndex = listPreference.findIndexOfValue(mSettingsPrefs.getStringFromRes(key, ""));
//      if (prefIndex >= 0) {
//        preference.setSummary(listPreference.getEntries()[prefIndex]);
//      }
//    } else {
//      preference.setSummary(mSettingsPrefs.getStringFromRes(key, ""));
//    }


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
          Constants.READ_LOCATION_PERMISSIONS_REQUEST);
    }
  }
}
