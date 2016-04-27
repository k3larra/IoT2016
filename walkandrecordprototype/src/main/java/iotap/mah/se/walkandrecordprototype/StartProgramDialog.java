package iotap.mah.se.walkandrecordprototype;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import iotap.mah.se.walkandrecordprototype.model.SoundTrack;

/**
 * Created by K3LARA on 2016-04-22.
 */
public class StartProgramDialog extends DialogFragment {
    private static final String TAG = "StartProgramDialog";
    private int mNum;
    private ArrayList soundTracks = new ArrayList<SoundTrack>();
    private Spinner spinner;
    private SoundTrack s;

    static StartProgramDialog newInstance(int num) {
        StartProgramDialog f = new StartProgramDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments().getInt("num");
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.start_dialog, container, false);
        //View tv = v.findViewById(R.id.text);
        //((TextView)tv).setText("Dialog #" + mNum + ": using style ");

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MapsActivity)getActivity()).setSoundTrackRef(s.getId());
                ((MapsActivity)getActivity()).walkingMode = true;
                ((MapsActivity)getActivity()).startedProgram = true;
                ((MapsActivity)getActivity()).startWalk();
                StartProgramDialog.this.dismiss();
            }
        });

        Button button2 = (Button)v.findViewById(R.id.record);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MapsActivity)getActivity()).walkingMode = false;
                ((MapsActivity)getActivity()).startedProgram = true;
                StartProgramDialog.this.dismiss();
                ((MapsActivity)getActivity()).startNewWalk();
            }
        });
        Log.i(TAG,"Number tracks  : "+ soundTracks.size());
        spinner = (Spinner) v.findViewById(R.id.selectWalk);
        ((MapsActivity)getActivity()).myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    SoundTrack track = postSnapshot.getValue(SoundTrack.class);
                    track.setId(postSnapshot.getKey());
                    soundTracks.add(track);
                }
                ((ArrayAdapter<SoundTrack>)spinner.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        ArrayAdapter<SoundTrack> adapter = new ArrayAdapter<SoundTrack>(getActivity(), android.R.layout.simple_spinner_item, soundTracks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   s = (SoundTrack)parent.getSelectedItem();

               }
               @Override
               public void onNothingSelected(AdapterView<?> parent) {

               }
           });
        getDialog().setTitle("SoundTrack");
        return v;
    }
}
