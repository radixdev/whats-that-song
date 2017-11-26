package com.radix.nowplayinglog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.radix.nowplayinglog.fragments.SettingsFragment;
import com.radix.nowplayinglog.fragments.SongListFragment;
import com.radix.nowplayinglog.fragments.SongMapFragment;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.GoogleDriveBackupHandler;
import com.radix.nowplayinglog.util.PermissionUtils;

public class MainActivity extends AppCompatActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener, SongListFragment.OnSongMapIconPressedListener,
    SettingsFragment.OnSettingsButtonClickedListener {

  private static final String TAG = MainActivity.class.getName();
  private ViewPager mViewPager;

  private BottomNavigationView mBottomNavigation;
  private GoogleDriveBackupHandler mGoogleDriveBackupHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Remove title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(R.layout.activity_main);

    mViewPager = findViewById(R.id.viewPager);
    mViewPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
    mViewPager.setOffscreenPageLimit(2);

    mBottomNavigation = findViewById(R.id.navigation);
    mBottomNavigation.setOnNavigationItemSelectedListener(this);
//    mBottomNavigation.setSelectedItemId(R.id.navigation_all_songs);
    mBottomNavigation.setSelectedItemId(R.id.navigation_settings);

    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        switch (position) {
          case 0:
            mBottomNavigation.setSelectedItemId(R.id.navigation_map);
            break;

          case 1:
            mBottomNavigation.setSelectedItemId(R.id.navigation_all_songs);
            break;

          case 2:
            mBottomNavigation.setSelectedItemId(R.id.navigation_settings);
            break;
        }
      }

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageScrollStateChanged(int state) {}
    });

    if (!PermissionUtils.isNotificationListenerEnabled(this)) {
      // TODO: 11/18/2017 show a dialog window here instead of just jumping straight there
      startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), 0);
    }

    mGoogleDriveBackupHandler = new GoogleDriveBackupHandler(this);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.navigation_map:
        mViewPager.setCurrentItem(0);
        return true;
      case R.id.navigation_all_songs:
        mViewPager.setCurrentItem(1);
        return true;
      case R.id.navigation_settings:
        mViewPager.setCurrentItem(2);
        return true;
    }
    return false;
  }

  @Override
  public void onSongMapClicked(Song song) {
    // Tell the map fragment to open the song on the map
    Log.d(TAG, "On Song map clicked " + song);

    mBottomNavigation.setSelectedItemId(R.id.navigation_map);

    SongMapFragment songMapFragment = ((ScreenSlidePagerAdapter) (mViewPager.getAdapter())).getMapFragment();
    if (songMapFragment != null) {
      songMapFragment.centerMapOnSong(song);
    }
  }

  @Override
  public void onGoogleDriveBackupClicked() {
    Log.d(TAG, "On Google Drive backup clicked");
    mGoogleDriveBackupHandler.startBackup();
  }

  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private SongMapFragment mMapFragment;

    ScreenSlidePagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          mMapFragment = new SongMapFragment();
          return mMapFragment;

        case 1:
          return new SongListFragment();

        case 2:
          return new SettingsFragment();
      }

      return new SongListFragment();
    }

    @Override
    public int getCount() {
      return 3;
    }

    SongMapFragment getMapFragment() {
      return mMapFragment;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    if (requestCode == Constants.READ_LOCATION_PERMISSIONS_REQUEST) {
      if (grantResults.length == 1 &&
          grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Read Location permission granted", Toast.LENGTH_SHORT).show();
      } else {
        // showRationale = false if user clicks Never Ask Again, otherwise true
        boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

        if (showRationale) {
          // do something here to handle degraded mode
        } else {
          Toast.makeText(this, "Read Location permission denied", Toast.LENGTH_SHORT).show();
        }
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mGoogleDriveBackupHandler.onActivityResult(requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
  }
}
