package iotap.mah.se.walkandrecordprototype;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by K3LARA on 2016-04-26.
 */
public class CreateNewWalk extends DialogFragment {
    EditText name;
    EditText author;
    static DialogFragment newInstance() {
        DialogFragment f = new CreateNewWalk();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_walk, container, false);
        name = (EditText)v.findViewById(R.id.et_name);
        author = (EditText)v.findViewById(R.id.et_author);
        Button b = (Button)v.findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //View v2 = getActivity().getLayoutInflater().inflate(R.layout.new_walk, null);
                ((MapsActivity)getActivity()).createNewWalk(name.getText().toString(),author.getText().toString());
                CreateNewWalk.this.dismiss();
            }
        });
        return v;
    }
}
