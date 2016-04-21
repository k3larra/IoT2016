package iotap.mah.se.audiotest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private MediaPlayerStreaming mediaPlayerStreaming;
    private static ArrayList<ToggleButton> toggleButtons = new ArrayList<ToggleButton>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t = (TextView)findViewById(R.id.textView10);
        t.setMovementMethod(new ScrollingMovementMethod());
        ((ToggleButton)findViewById(R.id.toggleButton1)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton2)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton3)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton4)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton5)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton6)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton7)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton8)).setOnCheckedChangeListener(this);

        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton1));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton2));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton3));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton4));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton5));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton6));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton7));
        toggleButtons.add((ToggleButton)findViewById(R.id.toggleButton8));

        mediaPlayerStreaming = new MediaPlayerStreaming(getString(R.string.Palme),this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        turnOffAllExceptThis(id);
        switch(id) {
            case R.id.toggleButton1:   //Low Volume
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(0.01f,0.01f);
                    mediaPlayerStreaming.playFromTo(20*1000, 30*1000);
                    setInfo("Low Volume");
                } else {
                    setInfo("Walking 1 off");
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton2:  //High Volume
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(1.0f,1.0f);
                    mediaPlayerStreaming.playFromTo(30*1000, 40*1000);
                    setInfo("High Volume");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton3:  //Right ear
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(0.0f,1.0f);
                    mediaPlayerStreaming.playFromTo(40*1000, 50*1000);
                    setInfo("Right Ear");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton4:  //Left Ear
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(1.0f,0.0f);
                    mediaPlayerStreaming.playFromTo(40*1000, 50*1000);
                    setInfo("Left Ear");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton5:  //Turn Right
                if (isChecked) {
                    mediaPlayerStreaming.playFromTo(40*1000, 60*1000);
                    mediaPlayerStreaming.turnRight();
                    setInfo("Turn Right");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton6:  //Turn Left
                if (isChecked) {
                    mediaPlayerStreaming.playFromTo(40*1000, 60*1000);
                    mediaPlayerStreaming.turnLeft();
                    setInfo("Turn Left");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton7:  //Turn Left sharp
                if (isChecked) {
                    mediaPlayerStreaming.playFromTo(40*1000, 50*1000);
                    mediaPlayerStreaming.turnLeftSharp();
                    setInfo("Turn Left Sharp");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton8:  //Slalom
                if (isChecked) {
                    mediaPlayerStreaming.playFromTo(40*1000, 300*1000);
                    mediaPlayerStreaming.slalom();
                    setInfo("Slalom");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            default:
                break;
        }

    }

    void setInfo(String s){
       TextView t = (TextView)findViewById(R.id.textView10);
        CharSequence all = t.getText();
        t.setText(s+"\n"+all);
    }

    void turnOffAllExceptThis(int id){
        for (ToggleButton v:toggleButtons) {
            if (v.getId()!=id){
                v.setChecked(false);
            }
        }
    }
}
