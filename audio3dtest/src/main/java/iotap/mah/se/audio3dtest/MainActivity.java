package iotap.mah.se.audio3dtest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String SOUND_FILE = "cube_sound.wav";
    //private static final String SOUND_FILE2 = "palme.mp3";
    //private static final String SOUND_FILE3 = "yahoo.mp3";
    //private static final String SOUND_FILE4 = "barbed.wav";
    private static final String SOUND_FILE5 = "test.wav";
    private CardboardAudioEngine cardboardAudioEngine;
    private volatile int soundId = CardboardAudioEngine.INVALID_ID;
    private float[] modelPosition;
    private float[] headRotation;
    private static final float MAX_MODEL_DISTANCE = 7.0f;
    private float yrotation =0;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //yrotation = Math.sin(yrotation + (float)Math.PI/180);
            updateHeadPosition();
            Log.i("MainActivity","Updateeeeeee");
        }
    };
    private class HeadUpdater extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Model first appears directly in front of user.
        modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
        headRotation = new float[] {0.0f, 0.0f, 0.0f,0.0f};
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        cardboardAudioEngine =
                new CardboardAudioEngine(this, CardboardAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        //cardboardAudioEngine.
        // Avoid any delays during start-up due to decoding of sound files.
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Start spatial audio playback of SOUND_FILE at the model postion. The returned
                        //soundId handle is stored and allows for repositioning the sound object whenever
                        // the cube position changes.
                        cardboardAudioEngine.preloadSoundFile(SOUND_FILE);
                        soundId = cardboardAudioEngine.createSoundObject(SOUND_FILE);
                        cardboardAudioEngine.setSoundObjectPosition(
                                soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
                        cardboardAudioEngine.playSound(soundId, true /* looped playback */);
                    }
                })
                .start();

        new Timer().schedule(new HeadUpdater(),50, 100);
    }

    void upDateSoundPosistion() {
        if (soundId != CardboardAudioEngine.INVALID_ID) {
            cardboardAudioEngine.setSoundObjectPosition(
                    soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
        }
    }

    void updateHeadPosition() {
        cardboardAudioEngine.setHeadRotation(
                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
        // Regular update call to cardboard audio engine.
        cardboardAudioEngine.update();
    }

    @Override
    public void onPause() {
        cardboardAudioEngine.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        cardboardAudioEngine.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
