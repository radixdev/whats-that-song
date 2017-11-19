package com.radix.nowplayinglog.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Set;

public class PermissionUtils {
  public static boolean isPermissionGranted(Context context, String permission) {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  }

  public static boolean isNotificationListenerEnabled(Context context) {
    String currentPackageName = context.getPackageName();
    final Set<String> enabledListenerPackages = NotificationManagerCompat.getEnabledListenerPackages(context);

    return enabledListenerPackages.contains(currentPackageName);
  }
}
