package com.radix.nowplayinglog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.radix.nowplayinglog.fragments.SettingsFragment;
import com.radix.nowplayinglog.fragments.SongListFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

  private ViewPager mViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mViewPager = findViewById(R.id.viewPager);
    mViewPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
    mViewPager.setCurrentItem(1);

    final BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(this);
    navigation.setSelectedItemId(R.id.navigation_all_songs);

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

  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    ScreenSlidePagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          // Return a map here!
          return new SongListFragment();

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
}
