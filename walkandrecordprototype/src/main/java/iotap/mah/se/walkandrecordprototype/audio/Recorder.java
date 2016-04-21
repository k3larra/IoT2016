package iotap.mah.se.walkandrecordprototype.audio;

import android.app.Activity;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.IOException;
/**
 * Created by K3LARA on 2016-04-21.
 */
public class Recorder{
    /*
 * The application needs to have the permission to write to external storage
 * if the output file is written to the external storage, and also the
 * permission to record audio. These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
 */
        private static final String LOG_TAG = "AudioRecordTest";
        private static String mFileName = null;
        private MediaRecorder mRecorder = null;
        private MediaPlayer   mPlayer = null;

    public Recorder() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

    }

    public Recorder(String fileName) {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/fileName.3gp";
    }

        public void startPlaying() {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }

        private void stopPlaying() {
            mPlayer.release();
            mPlayer = null;
        }

        public void startRecording() {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            mRecorder.start();
        }

        public void stopRecording() {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

