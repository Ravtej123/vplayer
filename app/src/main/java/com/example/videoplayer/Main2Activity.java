package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.io.File;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_VIDEO_REQUEST = 234;
    private ImageView imageView;
    private Button buttonChoose, buttonUpload;

    private String TAG = getClass().getName();

    //private Uri filePath;
    private ArrayList<MediaFile> mediaFileList;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        storageReference = FirebaseStorage.getInstance().getReference();

        imageView = (ImageView) findViewById(R.id.imageView);
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        mediaFileList = new ArrayList<>();
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    private void uploadFile() {
/*
        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading....");
            progressDialog.show();


            StorageReference riversRef = storageReference.child("videos/video1.mp4");

            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();


                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage(((int) progress) + "% Uploaded....");
                        }
                    });


        } else {
            //display error toast
        }*/


        if (mediaFileList != null && !mediaFileList.isEmpty()) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading....");
            progressDialog.show();


            StorageReference riversRef = storageReference.child("videos");
            for (MediaFile mediaFile : mediaFileList) {
                riversRef.child(mediaFile.getFileName()).putFile(mediaFile.getFileUri())
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();


                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressDialog.setMessage(((int) progress) + "% Uploaded....");
                            }
                        });
            }
        } else {
            //display error toast
            Toast.makeText(this, "Failed to upload files", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK) {
            /*filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            if (mediaFileList == null)
                mediaFileList = new ArrayList<>();
            else
                mediaFileList.clear();

            Log.e(getClass().getName(), "Request Code matched");
            if (data != null) {
                Log.e(getClass().getName(), "Data is not null");

                // Checking for selection multiple files or single.
                if (data.getClipData() != null) {
                    Log.e(getClass().getName(), "Data has clipData");

                    // Getting the length of data and logging up the logs using index
                    for (int index = 0; index < data.getClipData().getItemCount(); index++) {

                        // Getting the URIs of the selected files and logging them into logcat at debug level
                        Uri uri = data.getClipData().getItemAt(index).getUri();
                        Log.e("filesUri [" + uri + "] : ", String.valueOf(uri));

                        mediaFileList.add(new MediaFile(getFileName(this, uri), uri));
                    }
                } else {
                    Log.e(getClass().getName(), "Data has single file");

                    // Getting the URI of the selected file and logging into logcat at debug level
                    Uri uri = data.getData();
                    Log.e("fileUri: ", String.valueOf(uri));

                    mediaFileList.add(new MediaFile(getFileName(this, uri), uri));
                }

            } else {
                Log.e(getClass().getName(), "Error getting Pick Code");
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view == buttonChoose) {
            if (isStoragePermissionGranted())
                showFileChooser();

        } else if (view == buttonUpload) {

            uploadFile();

        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(getClass().getName(), "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private String getFileName(Context context, Uri uri) {
        String displayName = "";
        if (uri != null) {
            String uriString = uri.toString();
            if (uriString.startsWith("content://")) {
                try (Cursor cursor = context.getContentResolver()
                        .query(uri, null, null, null, null)) {
                    //Log.e(javaClass.name, "CursorInfo: $cursor")
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            } else if (uriString.startsWith("file://")) {
                displayName = new File(uriString).getName();
            }
        }
        return displayName;
    }
}