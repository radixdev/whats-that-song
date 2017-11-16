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

import com.radix.nowplayinglog.fragments.SongListFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

  private ViewPager mViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mViewPager = findViewById(R.id.viewPager);
    mViewPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));

    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(this);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.navigation_home:
        return true;
      case R.id.navigation_dashboard:
        return true;
      case R.id.navigation_notifications:
        return true;
    }
    return false;
  }

  /**
   * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
   * sequence.
   */
  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    public ScreenSlidePagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      return new SongListFragment();
    }

    @Override
    public int getCount() {
      return 3;
    }
  }
}
