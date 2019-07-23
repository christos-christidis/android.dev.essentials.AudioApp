package com.devessentials.audioapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101;
    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102;

    private Button mPlayButton;
    private Button mRecordButton;
    private Button mStopButton;

    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;

    private String mAudioFilePath;
    private boolean mIsRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayButton = findViewById(R.id.button_play);
        mRecordButton = findViewById(R.id.button_record);
        mStopButton = findViewById(R.id.button_stop);

        audioSetup();
    }

    private void audioSetup() {
        mPlayButton.setEnabled(false);
        mStopButton.setEnabled(false);

        if (!hasMicrophone()) {
            mRecordButton.setEnabled(false);
        }

        // SOS: external storage = SD card storage!
        mAudioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myaudio.3gp";

        requestPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
    }

    private boolean hasMicrophone() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    public void playAudio(View view) {
        mPlayButton.setEnabled(false);
        mRecordButton.setEnabled(false);
        mStopButton.setEnabled(true);

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mAudioFilePath);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    public void recordAudio(View view) {
        mIsRecording = true;
        mStopButton.setEnabled(true);
        mPlayButton.setEnabled(false);
        mRecordButton.setEnabled(false);

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(mAudioFilePath);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();
    }
    // SOS: Stop button can be used to stop a recording OR to stop playback of a recorded audio!

    public void stopAudio(View view) {
        mStopButton.setEnabled(false);
        mPlayButton.setEnabled(true);

        if (mIsRecording) {
            mRecordButton.setEnabled(false);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mIsRecording = false;
        } else {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mRecordButton.setEnabled(true);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    // SOS: we request Record Audio permission first, then if successful, the other permission too
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_PERMISSION_REQUEST_CODE:
                if (permission_not_granted(grantResults)) {
                    mRecordButton.setEnabled(false);
                    Toast.makeText(this, "Record Audio permission required", Toast.LENGTH_LONG).show();
                } else {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                }
                break;
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (permission_not_granted(grantResults)) {
                    mRecordButton.setEnabled(false);
                    Toast.makeText(this, "External Storage permission required", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean permission_not_granted(@NonNull int[] grantResults) {
        return grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED;
    }
}
