package com.radix.nowplayinglog.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.PermissionUtils;
import com.radix.nowplayinglog.util.scrobble.ScrobblerHandler;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = SettingsFragment.class.getSimpleName();

  public interface OnSettingsButtonClickedListener {
    void onGoogleDriveBackupClicked();
  }

  private OnSettingsButtonClickedListener mSettingsButtonClickedCallback;
  private SharedPreferences mSettingsPrefs;
  private ScrobblerHandler mScrobblerHandler;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_preferences);
    mScrobblerHandler = new ScrobblerHandler(getContext());
    mSettingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    onSharedPreferenceChanged(mSettingsPrefs, getString(R.string.settings_map_key));
    onSharedPreferenceChanged(mSettingsPrefs, getString(R.string.settings_music_player_key));

    findPreference(getString(R.string.settings_google_drive_backup_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        mSettingsButtonClickedCallback.onGoogleDriveBackupClicked();
        return true;
      }
    });
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Preference preference = findPreference(key);

    if (preference instanceof CheckBoxPreference) {
      CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
      if (key.equals(getString(R.string.settings_map_key))) {
        // Ask for the permission if needed
        if (checkBoxPreference.isChecked()) {
          // TODO: 11/27/2017 Uncheck the box if this is revoked!
          getPermissionToReadLocation();
        }
      } else if (key.equals(getString(R.string.settings_scrobble_app_key))) {
        // Show a dialog if they don't have the app
        if (checkBoxPreference.isChecked() && !mScrobblerHandler.isScrobblerAppInstalled()) {
          checkBoxPreference.setChecked(false);
          final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
          alert.setTitle(getString(R.string.enable_scrobble_app_title));
          alert.setMessage(getString(R.string.enable_scrobble_app_message));
          alert.setPositiveButton("Take me to the Play Store", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              mScrobblerHandler.takeUserToScrobbleApp();
              dialog.dismiss();
            }
          });
          alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
          alert.show();
        }
      } else if (key.equals(getString(R.string.settings_dark_theme_key))) {
        Log.i(TAG, "Dark theme toggled, restarting");
        // Restart the app
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        getContext().startActivity(intent);
      }
    } else if (preference instanceof ListPreference) {
      ListPreference listPreference = (ListPreference) preference;
      if (key.equals(getString(R.string.settings_music_player_key))) {
        String value = (String) listPreference.getEntry();
        preference.setSummary("Music player set to: " + value);
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
