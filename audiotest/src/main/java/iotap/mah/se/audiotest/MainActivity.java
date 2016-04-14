package iotap.mah.se.audiotest;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private MediaPlayerStreaming mediaPlayerStreaming;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t = (TextView)findViewById(R.id.textViewInfo);
        t.setMovementMethod(new ScrollingMovementMethod());
        ((ToggleButton)findViewById(R.id.toggleButton1)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton2)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton3)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton4)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton5)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.toggleButton6)).setOnCheckedChangeListener(this);

        mediaPlayerStreaming = new MediaPlayerStreaming(getString(R.string.Palme),this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch(id) {
            case R.id.toggleButton1:
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(0,100);
                    mediaPlayerStreaming.playFromTo(20*1000, 30*1000);
                    setInfo("1");
                } else {
                    setInfo("Walking 1 off");
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton2:
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(100,10);
                    mediaPlayerStreaming.playFromTo(30*1000, 40*1000);
                    setInfo("2");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            case R.id.toggleButton3:
                if (isChecked) {
                    mediaPlayerStreaming.setVolume(100,100);
                    mediaPlayerStreaming.playFromTo(40*1000, 50*1000);
                    setInfo("3");
                } else {
                    mediaPlayerStreaming.pause();
                }
                break;
            default:
                break;
        }

    }

    void setInfo(String s){
        TextView t = (TextView)findViewById(R.id.textViewInfo);
        t.append(s+"\n");
        //int scroll_amount = (int) (t.getLineHeight()*t.getLineCount());
        //t.scrollTo(0, 100000);
        //t.invalidate();
    }

}
