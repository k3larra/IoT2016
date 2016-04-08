package com.example.k3lara.assignment4;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.k3lara.assignment4.model.Track;

/**
 * Created by K3LARA on 2015-09-13.
 */
public class RestartDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("WRONG Answer");
        dialog.setMessage("You have to start all over again :).");
        dialog.setPositiveButton("Restart", new PositiveButton());
        dialog.setNegativeButton("Cancel", new QuitButton());
        return dialog.create();
    }
    private class PositiveButton implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Track.getInstance().resetTrack();
            dialog.dismiss();
        }
    }

    private class QuitButton implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }
}
