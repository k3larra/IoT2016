package iotap.mah.se.walkandrecordprototype;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by K3LARA on 2016-04-21.
 */
public class StartStopDialog extends DialogFragment {

    OnSelectionListener mListener;

    // Container Activity must implement this interface
    public interface OnSelectionListener {
        public void OnSelectionListener(int i);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        mListener = (OnSelectionListener)getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.startstop)
                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       mListener.OnSelectionListener(0);
                    }
                })
                .setNeutralButton(R.string.replay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.OnSelectionListener(1);
                    }
                })
                .setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.OnSelectionListener(2);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
