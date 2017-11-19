package com.radix.nowplayinglog.util;

import android.content.Context;
import android.content.pm.PackageManager;

public class LocationUtils {
  public static boolean isPermissionGranted(Context context, String permission) {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  }
}
