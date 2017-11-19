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
import android.widget.Toast;

import com.radix.nowplayinglog.fragments.SettingsFragment;
import com.radix.nowplayinglog.fragments.SongListFragment;
import com.radix.nowplayinglog.fragments.SongMapFragment;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.util.Constants;
import com.radix.nowplayinglog.util.PermissionUtils;

public class MainActivity extends AppCompatActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener, SongListFragment.OnSongMapIconPressedListener {

  private static final String TAG = MainActivity.class.getName();

  private ViewPager mViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mViewPager = findViewById(R.id.viewPager);
    mViewPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));

    final BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(this);
    navigation.setSelectedItemId(R.id.navigation_all_songs);
//    navigation.setSelectedItemId(R.id.navigation_settings);
//    navigation.setSelectedItemId(R.id.navigation_map);

    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        switch (position) {
          case 0:
            navigation.setSelectedItemId(R.id.navigation_map);
            break;

          case 1:
            navigation.setSelectedItemId(R.id.navigation_all_songs);
            break;

          case 2:
            navigation.setSelectedItemId(R.id.navigation_settings);
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
    Log.d(TAG, "Got a click " + song);
  }

  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    ScreenSlidePagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return new SongMapFragment();

        case 1:
          return new SongListFragment();

        case 2:
          return new SettingsFragment();
      }

      return null;
    }

    @Override
    public int getCount() {
      return 3;
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    // Make sure it's our original READ_CONTACTS request
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
}
