package iotap.mah.se.audiotest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by K3LARA on 2016-04-14.
 */
public class MediaPlayerStreaming implements MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener {
    private String streamingFile;
    private MediaPlayer mp;
    private MainActivity mainActivity;



    private boolean prepared;
    private int millisEnd =0;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //mainActivity.setInfo("Stopping at:"+ mp.getCurrentPosition() + " should stop at: " + millisEnd);
            if (mp.isPlaying()){
                mp.pause();
            }
        }
    };

    private class Sleeper extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    private int mediaFileLengthInMilliseconds;
    public MediaPlayerStreaming(String streamingFile, MainActivity c) {
        this.streamingFile = streamingFile;
        this.mp = new MediaPlayer();
        mp.setOnPreparedListener(this);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnSeekCompleteListener(this);
        mp.setOnErrorListener(this);
        mainActivity = c;
        try {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(streamingFile );
            mp.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            c.setInfo(e.getMessage());
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
        mediaFileLengthInMilliseconds = mp.getDuration();
        mainActivity.setInfo("OK prepared player ready for takeoff!!!!"+mp.getDuration());
    }

    public void playFrom(int millisStart){
        this.millisEnd = 0;
        if(mp.isPlaying()) {
            mp.pause();
        }
        if (prepared) {
            mp.seekTo(millisStart);
        }
    }

    public void playFromTo(int millisStart, int millisEnd){
        this.millisEnd = millisEnd;
        if(mp.isPlaying()) {
            mp.pause();
        }
        if (prepared) {
            mp.seekTo(millisStart);
        }
    }

    public void pause(){
        if (prepared) {
            if(mp.isPlaying()) {
                mp.pause();
            }
        }
    }

    public void trashPlayer(){
        mp.stop();
        mp.release();
        mp = null;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    public int getMediaFileLengthInMilliseconds() {
        if (prepared) {
            return mediaFileLengthInMilliseconds;
        }else{
            return 0;
        }
    }

    /**Note that the passed volume values are raw scalars in range 0.0 to 1.0. UI controls should be scaled logarithmically*/
    void setVolume(int leftVolume, int rightVolume){
        int maxVolume = 100;
        float left=(float)(Math.log(maxVolume-leftVolume)/Math.log(maxVolume));
        float right=(float)(Math.log(maxVolume-rightVolume)/Math.log(maxVolume));
        if (prepared) {
            mp.setVolume(1-left, 1-right);
        }
    }
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
            mp.start();
            int i = this.millisEnd - mp.getCurrentPosition();
            if (i>0) {
                new Timer().schedule(new Sleeper(), this.millisEnd - mp.getCurrentPosition());
            }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mainActivity.setInfo("Media Error");
        return false;
    }

    public boolean isPrepared() {
        return prepared;
    }

}
