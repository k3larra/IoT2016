package com.example.k3lara.assignment4;


import android.app.Dialog;
//import android.content.DialogInterface;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
//import android.app.Fragment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.k3lara.assignment4.model.Assignment;
import com.example.k3lara.assignment4.model.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuissDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{
    public static final String TAG = "QuissDialogFragment";
    public QuissDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Assignment a = Track.getInstance().getCurrentPoint().getAssignment();
        a.setState(Assignment.STATE.STARTED);
        String[] alternatives = {a.getAnswer(0),a.getAnswer(1),a.getAnswer(2)};
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setSingleChoiceItems(alternatives, 0, this);
        dialog.setTitle(a.getQuestion());
        dialog.setPositiveButton("OK", new PositiveButtonClickListener());
        MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), Track.getInstance().getCurrentPoint().getSound());
        mediaPlayer.start();
        Vibrator v = (Vibrator) getActivity().getSystemService(FragmentActivity.VIBRATOR_SERVICE);
        v.vibrate(100);
        return dialog.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.i(TAG, "Selected: " + which);
        //Track.getInstance().getCurrentPoint().assignmentAnswer(which);
    }


    class PositiveButtonClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
            Track
                    .getInstance()
                    .getCurrentPoint()
                    .getAssignment()
                    .setAnswer(selected);
            dialog.dismiss();
        }
    }



}
