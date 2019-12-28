package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class VideoActivity extends AppCompatActivity {

    private EditText videoPathEditor = null;

    private Button browseVideoFileButton = null;

    private Button playVideoButton = null;

    private Button stopVideoButton = null;

    private Button pauseVideoButton = null;

    private Button continueVideoButton = null;

    private Button replayVideoButton = null;

    private VideoView playVideoView = null;

    private ProgressBar videoProgressBar = null;

    // Request code for user select video file.
    private static final int REQUEST_CODE_SELECT_VIDEO_FILE = 1;

    // Request code for require android READ_EXTERNAL_PERMISSION.
    private static final int REQUEST_CODE_READ_EXTERNAL_PERMISSION = 2;

    // Used when update video progress thread send message to progress bar handler.
    private static final int UPDATE_VIDEO_PROGRESS_BAR = 3;

    // Save local video file uri.
    private Uri videoFileUri = null;

    // Wait update video progress thread sent message, then update video play progress.
    private Handler videoProgressHandler = null;

    // Save current video play position.
    private int currVideoPosition = 0;

    // Save whether the video is paused or not.
    private boolean isVideoPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        setTitle("dev2qa.com - Android Play Video Example.");

        // Init this example used video components.
        initVideoControls();

        // When user input video file url in the video file path edittext input text box.
        videoPathEditor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int action = keyEvent.getAction();
                if(action == KeyEvent.ACTION_UP) {
                    String text = videoPathEditor.getText().toString();
                    if (text.length() > 0) {
                        // If user input video file url, enable play button.
                        playVideoButton.setEnabled(true);
                        pauseVideoButton.setEnabled(false);
                        replayVideoButton.setEnabled(false);
                    } else {
                        // If user input nothing, disable all buttons.
                        playVideoButton.setEnabled(false);
                        pauseVideoButton.setEnabled(false);
                        replayVideoButton.setEnabled(false);
                    }
                }
                return false;
            }
        });

        // If user click browse video file button.
        browseVideoFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether user has granted read external storage permission to this activity.
                int readExternalStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

                // If not grant then require read external storage permission.
                if(readExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
                {
                    String requirePermission[] = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(VideoActivity.this, requirePermission, REQUEST_CODE_READ_EXTERNAL_PERMISSION);
                }else {
                    selectVideoFile();
                }
            }
        });

        // Click this button to play user browsed or input video file.
        playVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoFilePath = videoPathEditor.getText().toString();

                if(!TextUtils.isEmpty(videoFilePath))
                {
                    if(!videoFilePath.trim().toLowerCase().startsWith("http")) {
                        // Play local video file.
                        playVideoView.setVideoURI(videoFileUri);
                    }else
                    {
                        // Convert the web video url to a Uri object.
                        Uri webVideoFileUri = Uri.parse(videoFilePath.trim());

                        // Play web video file use the Uri object.
                        playVideoView.setVideoURI(webVideoFileUri);
                    }

                    playVideoView.setVisibility(View.VISIBLE);

                    videoProgressBar.setVisibility(ProgressBar.VISIBLE);

                    currVideoPosition = 0;

                    playVideoView.start();

                    playVideoButton.setEnabled(false);

                    stopVideoButton.setEnabled(true);

                    pauseVideoButton.setEnabled(true);

                    continueVideoButton.setEnabled(false);

                    replayVideoButton.setEnabled(true);
                }

            }
        });

        // Click this button to stop playing video file.
        stopVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Stop video playing.
                playVideoView.stopPlayback();

                // Seek to the beginning of the video file.
                playVideoView.seekTo(0);

                playVideoButton.setEnabled(true);

                stopVideoButton.setEnabled(false);

                pauseVideoButton.setEnabled(false);

                continueVideoButton.setEnabled(false);

                replayVideoButton.setEnabled(false);
            }
        });

        // Click this button to pause playing video file.
        pauseVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Pause video play.
                playVideoView.pause();

                isVideoPaused = true;

                // Record current video play position.
                currVideoPosition = playVideoView.getCurrentPosition();

                playVideoButton.setEnabled(false);

                stopVideoButton.setEnabled(true);

                pauseVideoButton.setEnabled(false);

                continueVideoButton.setEnabled(true);

                replayVideoButton.setEnabled(true);
            }
        });

        // Click this button to continue play paused video.
        continueVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                playVideoView.seekTo(currVideoPosition);

                playVideoButton.setEnabled(false);

                pauseVideoButton.setEnabled(true);

                stopVideoButton.setEnabled(true);

                continueVideoButton.setEnabled(false);

                replayVideoButton.setEnabled(true);
            }
        });

        // Click this button to replay video file.
        replayVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Replay video.
                playVideoView.resume();

                // Set current video play position to 0.
                currVideoPosition = 0;

                playVideoButton.setEnabled(false);

                pauseVideoButton.setEnabled(true);

                stopVideoButton.setEnabled(true);

                continueVideoButton.setEnabled(false);

                replayVideoButton.setEnabled(true);
            }
        });
    }

    /*
      Initialise play video example controls.
    * */
    private void initVideoControls()
    {
        if(videoPathEditor==null)
        {
            videoPathEditor = (EditText) findViewById(R.id.play_video_file_path_editor);
        }

        if(browseVideoFileButton==null)
        {
            browseVideoFileButton = (Button)findViewById(R.id.browse_video_file_button);
        }

        if(playVideoButton==null)
        {
            playVideoButton = (Button)findViewById(R.id.play_video_start_button);
        }

        if(stopVideoButton==null)
        {
            stopVideoButton = (Button)findViewById(R.id.play_video_stop_button);
        }

        if(pauseVideoButton==null)
        {
            pauseVideoButton = (Button)findViewById(R.id.play_video_pause_button);
        }

        if(continueVideoButton==null)
        {
            continueVideoButton = (Button)findViewById(R.id.play_video_continue_button);
        }

        if(replayVideoButton==null)
        {
            replayVideoButton = (Button)findViewById(R.id.play_video_replay_button);
        }

        if(playVideoView==null)
        {
            playVideoView = (VideoView)findViewById(R.id.play_video_view);
        }

        if(videoProgressBar==null)
        {
            videoProgressBar = (ProgressBar) findViewById(R.id.play_video_progressbar);
        }

        if(videoProgressHandler==null)
        {
            // This handler wait and receive update progress bar message from child thread.
            videoProgressHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    // When receive update progressbar message.
                    if(msg.what == UPDATE_VIDEO_PROGRESS_BAR)
                    {
                        // Get current video play position.
                        int currVideoPosition = playVideoView.getCurrentPosition();

                        // Get total video length.
                        int videoDuration = playVideoView.getDuration();

                        // Calculate the percentage.
                        int progressPercent = currVideoPosition * 100 / videoDuration;

                        // 10 times percentage value to make effect clear.
                        videoProgressBar.setProgress(progressPercent);
                    }
                }
            };

            // This thread send update video progress message to video progress Handler every 2 seconds.
            Thread updateProgressThread = new Thread()
            {
                @Override
                public void run() {

                    try {
                        while (true) {
                            // Create update video progressbar message.
                            Message msg = new Message();
                            msg.what = UPDATE_VIDEO_PROGRESS_BAR;

                            // Send the message to video progressbar update handler.
                            videoProgressHandler.sendMessage(msg);

                            // Sleep 2 seconds.
                            Thread.sleep(2000);
                        }
                    }catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };
            // Start the thread.
            updateProgressThread.start();
        }

        setContinueVideoAfterSeekComplete();
    }

    /* This method start get content activity to let user select video file from local directory.*/
    private void selectVideoFile()
    {
        // Create an intent with action ACTION_GET_CONTENT.
        Intent selectVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);

        // Show video in the content browser.
        // Set selectVideoIntent.setType("*/*") to select all data
        // Intent for this action must set content type, otherwise you will encounter below exception : android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.intent.action.GET_CONTENT }
        selectVideoIntent.setType("video/*");

        // Start android get content activity ( this is a android os built-in activity.) .
        startActivityForResult(selectVideoIntent, REQUEST_CODE_SELECT_VIDEO_FILE);
    }

    /* This method will be invoked when startActivityForResult method complete in selectVideoFile() method.
     *  It is used to process activity result that is started by startActivityForResult method.
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Identify activity by request code.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_VIDEO_FILE) {
            // If the request is success.
            if (resultCode == RESULT_OK) {
                // To make example simple and clear, we only choose video file from local file,
                // this is easy to get video file real local path.
                // If you want to get video file real local path from a video content provider
                // Please read another article.
                videoFileUri = data.getData();

                String videoFileName = videoFileUri.getLastPathSegment();

                videoPathEditor.setText("You select video file is " + videoFileName);

                playVideoButton.setEnabled(true);

                pauseVideoButton.setEnabled(false);

                replayVideoButton.setEnabled(false);
            }
        }
    }

    /* Run this method after user choose grant read external storage permission or not. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CODE_READ_EXTERNAL_PERMISSION)
        {
            if(grantResults.length > 0)
            {
                int grantResult = grantResults[0];
                if(grantResult == PackageManager.PERMISSION_GRANTED)
                {
                    // If user grant the permission then open browser let user select audio file.
                    selectVideoFile();
                }else
                {
                    Toast.makeText(getApplicationContext(), "You denied read external storage permission.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /* This method is used to play video after seek complete, otherwise video playing will not be accurate.*/
    private void setContinueVideoAfterSeekComplete()
    {
        playVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        if(isVideoPaused)
                        {
                            playVideoView.start();
                            isVideoPaused = false;
                        }
                    }
                });
            }
        });
    }
}












































   /* private Button btn;
    private VideoView videoView;
    private static final String VIDEO_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


        btn =findViewById(R.id.btn);
        videoView = findViewById(R.id.vv);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select video from gallery",
                "Record video from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                chooseVideoFromGallary();
                                break;
                            case 1:
                                takeVideoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void chooseVideoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takeVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("result",""+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            Log.d("what","cancel");
            return;
        }

        if (requestCode == GALLERY) {
            Log.d("what","gale");
            if (data != null) {
                Uri contentURI = data.getData();

                String selectedVideoPath = getPath(contentURI);
                Log.d("path",selectedVideoPath);
                saveVideoToInternalStorage(selectedVideoPath);
                videoView.setVideoURI(contentURI);
                videoView.requestFocus();
                videoView.start();

            }

        } else if (requestCode == CAMERA) {
            Uri contentURI = data.getData();
            String recordedVideoPath = getPath(contentURI);
            Log.d("frrr",recordedVideoPath);
            saveVideoToInternalStorage(recordedVideoPath);
            videoView.setVideoURI(contentURI);
            videoView.requestFocus();
            videoView.start();
        }
    }

    private void saveVideoToInternalStorage (String filePath) {

        File newfile;

        try {

            File currentFile = new File(filePath);
            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY);
            newfile = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".mp4");

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            if(currentFile.exists()){

                InputStream in = new FileInputStream(currentFile);
                OutputStream out = new FileOutputStream(newfile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                Log.v("vii", "Video file saved successfully.");
            }else{
                Log.v("vii", "Video saving failed. Source file missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }*/
