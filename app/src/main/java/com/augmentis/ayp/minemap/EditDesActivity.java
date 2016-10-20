package com.augmentis.ayp.minemap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.augmentis.ayp.minemap.model.MineLocation;
import com.bumptech.glide.Glide;

public class EditDesActivity extends AppCompatActivity {

    private static final String TAG = "EditDesActivity";

    protected EditText mInputName;
    protected EditText mInputTel;
    protected EditText mInputDes;
    protected EditText mInputOpen;
    protected EditText mInputClose;

    protected MineLocation mineLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_description);


        mineLocation = MineLocation.getInstance();

        ImageView img = (ImageView) findViewById(R.id.imageView2);
        Glide.with(this).load(R.drawable.map_description).into(img);

        Log.d(TAG, "lat, lng : " + mineLocation.getLatitude() + ", " +
                mineLocation.getLongitude() + ", " + mineLocation.getType());

        mInputName = (EditText) findViewById(R.id.input_name);
        mInputTel = (EditText) findViewById(R.id.input_tel);
        mInputDes = (EditText) findViewById(R.id.input_des);
        mInputOpen = (EditText) findViewById(R.id.edtOpen);
        mInputClose = (EditText) findViewById(R.id.edtClose);

        Button mButtonSave = (Button) findViewById(R.id.btn_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /////
            }
        });

    }
}
