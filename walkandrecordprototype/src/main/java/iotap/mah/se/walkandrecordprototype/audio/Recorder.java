package iotap.mah.se.walkandrecordprototype.audio;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import iotap.mah.se.walkandrecordprototype.Constants;

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
        private static final String TAG = "AudioRecordTest";
        private static String mFileName = null;
        private MediaRecorder mRecorder = null;
        private MediaPlayer   mPlayer = null;
        private static String fireBaseStore = null;
        private static String fileID;
        private Context c;

    public Recorder() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

    }

    public Recorder(String fileName, Context c) {
        this.c = c;
        fileID = fileName;
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/"+fileName+".3gp";
        fireBaseStore = "https://soundtrackmalmo.firebaseapp.com/"+fileName+".3gp";
    }

        public void startPlaying() {
            mPlayer = new MediaPlayer();
            Log.e(TAG, " mFileName");
            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
        }

        public void startStreaming() {
            mPlayer = new MediaPlayer();
            Log.i(TAG, " mFileName"+fireBaseStore);
            try {
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource( fireBaseStore);
                mPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stopPlaying() {
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
                Log.e(TAG, "prepare() failed");
            }

            mRecorder.start();
        }

        public void stopRecording() {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        public void saveRecordingToCloud(){
            //Login
            if (mRecorder == null) {
                new Timer().schedule(new FTPUploader(), 0);
            }
        }


    //For stopping the sequence
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(c,"Uploaded finished: ",Toast.LENGTH_LONG);
        }
    };
    private class FTPUploader extends TimerTask {

        @Override
        public void run() {
            FTPClient ftp = new FTPClient();
            FTPClientConfig config = new FTPClientConfig();

            //config.setsetXXX(YYY); // change required options
            // for example config.setServerTimeZoneId("Pacific/Pitcairn")
            ftp.configure(config );
            boolean error = false;
            try {
                int reply;
                String server = "ftp.ixdk3.com";
                ftp.connect(server);
                boolean status = ftp.login(Constants.loginNameGodaddy,Constants.passwordGodaddy);
                Log.i(TAG,"FTP Login: "+status);
                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    Log.i(TAG,"FTP server refused connection.");
                }
                //... // transfer files
                try {
                    //ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
                    //Log.e(TAG, "Trying to upload: "+mFileName);
                    //ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
                    ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                    FileInputStream srcFileStream = new FileInputStream(mFileName);
                   //boolean statusUpload = ftp.storeFile(mFileName,new BufferedInputStream(srcFileStream));
                    boolean statusUpload = ftp.storeFile(fileID+Constants.SOUND_FILE_EXTENSION,srcFileStream);
                    srcFileStream.close();
                    Log.e(TAG, "Uploaded: "+String.valueOf(statusUpload)+" "+mFileName);
                } catch (Exception e) {
                    Log.e(TAG, "Uploaded failed"+e.getMessage());
                }
                ftp.logout();
            } catch(IOException e) {
                error = true;
                Log.i(TAG,"IO"+e.toString());
            } finally {
                if(ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch(IOException ioe) {
                        // do nothing
                    }
                }
            }
            handler.sendEmptyMessage(0);
        }
    }
    }

