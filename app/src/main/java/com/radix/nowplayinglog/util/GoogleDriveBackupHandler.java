package com.radix.nowplayinglog.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.radix.nowplayinglog.R;
import com.radix.nowplayinglog.models.Song;
import com.radix.nowplayinglog.storage.SongStorageThing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static android.app.Activity.RESULT_OK;

public class GoogleDriveBackupHandler {
  private static final String TAG = GoogleDriveBackupHandler.class.getName();
  /**
   * Request code for google sign-in
   */
  private static final int REQUEST_CODE_SIGN_IN = 2;

  private static final int REQUEST_CODE_CREATE_FILE = 1;

  private final Activity mActivity;
  private final Context mContext;

  /**
   * Handles high-level drive functions like sync
   */
  private DriveClient mDriveClient;

  /**
   * Handle access to Drive resources/files.
   */
  private DriveResourceClient mDriveResourceClient;

  private final SongStorageThing mSongStorage;

  public GoogleDriveBackupHandler(Activity activity) {
    mActivity = activity;
    mContext = mActivity.getApplicationContext();
    mSongStorage = new SongStorageThing(mContext);
  }

  /**
   * Callback for the real {@link Activity#onActivityResult(int, int, Intent)}
   */
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CODE_SIGN_IN:
        if (resultCode != RESULT_OK) {
          // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
          // required and is fatal. For apps where sign-in is optional, handle
          // appropriately
          Log.e(TAG, "Sign-in failed.");
          return;
        }

        Task<GoogleSignInAccount> getAccountTask =
            GoogleSignIn.getSignedInAccountFromIntent(data);
        if (getAccountTask.isSuccessful()) {
          initializeDriveClient(getAccountTask.getResult());
          backupSongsTask(mDriveResourceClient, mDriveClient);
        } else {
          Log.e(TAG, "Sign-in failed.");
        }
        break;
    }
  }

  public void startBackup() {
    Log.i(TAG, "Starting sign in");
    if (mSongStorage.isStorageEmpty()) {
      showMessage("No songs to backup!");
      return;
    }
    GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
    mActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
  }

  private void backupSongsTask(DriveResourceClient driveResourceClient, final DriveClient driveClient) {
    Task<DriveContents> createContentsTask = driveResourceClient.createContents();
    createContentsTask
        .continueWithTask(new Continuation<DriveContents, Task<IntentSender>>() {
          @Override
          public Task<IntentSender> then(@NonNull Task<DriveContents> task)
              throws Exception {
            DriveContents contents = task.getResult();
            OutputStream outputStream = contents.getOutputStream();
            try (Writer writer = new OutputStreamWriter(outputStream)) {
              writeSongDataToFile(writer);
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("Now Playing Log Backup " + mSongStorage.getNumberOfStoredSongsForDisplay())
                .setMimeType("text/plain")
                .build();

            CreateFileActivityOptions createOptions =
                new CreateFileActivityOptions.Builder()
                    .setInitialDriveContents(contents)
                    .setInitialMetadata(changeSet)
                    .build();
            return driveClient.newCreateFileActivityIntentSender(createOptions);
          }
        })
        .addOnSuccessListener(mActivity,
            new OnSuccessListener<IntentSender>() {
              @Override
              public void onSuccess(IntentSender intentSender) {
                try {
                  mActivity.startIntentSenderForResult(
                      intentSender, REQUEST_CODE_CREATE_FILE, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                  Log.e(TAG, "Unable to create file", e);
                  showMessage(mContext.getString(R.string.file_create_error));
                }
              }
            })
        .addOnFailureListener(mActivity, new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Log.e(TAG, "Unable to create file", e);
            showMessage(mContext.getString(R.string.file_create_error));
          }
        });
  }

  private void writeSongDataToFile(Writer writer) throws IOException {
    for (Song song : mSongStorage.getAllSongs()) {
      String songLine = song.getTitleAndArtistForDisplay() + " on " + song.getPrettyDate();
      if (song.hasLocationSet()) {
        songLine += " at " + song.getLatLngForDisplay();
      }

      writer.write(songLine);
      writer.write("\n");
    }
  }

  private void initializeDriveClient(GoogleSignInAccount signInAccount) {
    mDriveClient = Drive.getDriveClient(mContext, signInAccount);
    mDriveResourceClient = Drive.getDriveResourceClient(mContext, signInAccount);
  }

  private GoogleSignInClient buildGoogleSignInClient() {
    GoogleSignInOptions signInOptions =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_FILE)
            .build();
    return GoogleSignIn.getClient(mContext, signInOptions);
  }

  private void showMessage(String msg) {
    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
  }
}
