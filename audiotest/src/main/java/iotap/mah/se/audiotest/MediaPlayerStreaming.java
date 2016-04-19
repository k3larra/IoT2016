package iotap.mah.se.audiotest;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by K3LARA on 2016-04-14.
 */
public class MediaPlayerStreaming implements MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener {
    private String streamingFile;
    private MediaPlayer mp;
    private MainActivity mainActivity;
    private Timer t, turnTester;
    private boolean prepared;
    private int millisEnd =0;
    private double degrees = 0.0;

    //For stopping the sequence
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
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

   //For Handling turns
    Handler turnHandler = new Handler(){
        private double left = 0.0f;
        private double right = 1.0f;

        double radians = Math.toRadians(degrees);
        @Override
        public void handleMessage(Message msg) {
            //mainActivity.setInfo("Stopping at:"+ mp.getCurrentPosition() + " should stop at: " + millisEnd);
            radians = Math.toRadians(degrees);
            left = Math.abs(Math.sin(radians));
            right = Math.abs(Math.cos(radians));
            mp.setVolume((float) left, (float) right);
            if (degrees >= 360) {
                degrees = 0;
            }
            Log.i("MediaPlayerStreaming", "Degrees: " + degrees);
            degrees = degrees + 0.2;
            //0 = Left, 1 = right, 2 =left sharp, 3 = rightSharp 4 = slalom
            if (msg.what==0) {  //left that is from 135 degrees to 225
                Log.i("MediaPlayerStreaming","Message: left");
                if (degrees > 135){
                    turnTester.cancel();
                }
                degrees = degrees + 0.2;
            }

            if (msg.what==1) { //right
                Log.i("MediaPlayerStreaming","Message: right");
                if (degrees > 225){
                    turnTester.cancel();
                }
            }

            if (msg.what==2) { //Left sharp
                Log.i("MediaPlayerStreaming","Message: left sharp");
                if (degrees > 135){
                    turnTester.cancel();
                }
                degrees = degrees + 0.2;
            }
            if (msg.what==3) {  //right sharp
                Log.i("MediaPlayerStreaming","Message: right sharp");
                if (degrees > 225){
                    turnTester.cancel();
                }
                degrees = degrees + 0.4;
            }

            if (msg.what==4) {
                Log.i("MediaPlayerStreaming","Message: slalom");
            }
        }
    };
    private class TurnTimer extends TimerTask {
        private int type = 0;  //0 = Left, 1 = right, 2 =left sharp, 3 = rightSharp 4 = slalom
        public TurnTimer(int type) {
            this.type = type;
        }

        @Override
        public void run() {
            turnHandler.sendEmptyMessage(type);

        }
    }


    public void slalom() {
        degrees =45;
        turnTester = new Timer();
        turnTester.schedule(new TurnTimer(4),0,40);
    }

    //Pause 5 sec then make a turn in 5 sec
    public void turnRight() {
        degrees =135;
        turnTester = new Timer();
        turnTester.schedule(new TurnTimer(1),5000,40);

    }

    public void turnLeft() {
        degrees =45;
        turnTester = new Timer();
        turnTester.schedule(new TurnTimer(0),5000,40);
    }

    public void turnLeftSharp() {
        degrees =45;
        turnTester = new Timer();
        turnTester.schedule(new TurnTimer(2),2000,40);
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
        try{
            t.cancel();
            turnTester.cancel();
        }catch(Exception e){}
        if(mp.isPlaying()) {
            mp.pause();
        }
        if (prepared) {
            mp.seekTo(millisStart);
        }
    }

    public void playFromTo(int millisStart, int millisEnd){
        try{
            t.cancel();
            turnTester.cancel();
        }catch(Exception e){}
        this.millisEnd = millisEnd;
        if(mp.isPlaying()) {
            mp.pause();
        }
        if (prepared) {
            mp.seekTo(millisStart);
        }
    }

    public void pause(){
        try{
            t.cancel();
            turnTester.cancel();
        }catch(Exception e){}
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
    void setVolume(float leftVolume, float rightVolume){
        //int maxVolume = 100;
        //float left=(float)(Math.log(maxVolume-leftVolume)/Math.log(maxVolume));
        //float right=(float)(Math.log(maxVolume-rightVolume)/Math.log(maxVolume));
        if (prepared) {
            mp.setVolume(leftVolume, rightVolume);
            Log.i("MediaPlayerStreaming","Left: "+leftVolume+" Right: "+rightVolume);
        }
    }
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
            mp.start();

            int i = this.millisEnd - mp.getCurrentPosition();
            Log.i("MediaPlayerStreaming","Length: "+i+" current "+ mp.getCurrentPosition()+" End "+this.millisEnd);
            if (i>0) {
                t = new Timer();
                t.schedule(new Sleeper(), this.millisEnd - mp.getCurrentPosition());
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
