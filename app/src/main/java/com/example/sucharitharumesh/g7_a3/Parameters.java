package com.example.sucharitharumesh.g7_a3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Parameters extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        Bundle bundle = getIntent().getExtras();
        String acc = bundle.getString("accuracy");
        TextView kcrossval = (TextView)findViewById(R.id.kcrossval);
        kcrossval.setText(acc);
    }
}
