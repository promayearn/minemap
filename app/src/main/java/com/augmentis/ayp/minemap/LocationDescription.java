package com.augmentis.ayp.minemap;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.augmentis.ayp.minemap.model.MineLocation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationDescription extends AppCompatActivity {

    private static String TAG = "LocationDescription";

    private EditText mInputName;
    private EditText mInputDate;
    private EditText mInputTel;
    private EditText mInputDes;

    private Button mButtonSave;

    private MineLocation mineLocation;

    private String loc_name;
    private String loc_date;
    private String loc_tel;
    private String loc_des;
    private String loc_lat;
    private String loc_long;
    private String id_user;
    private String loc_type;
    private String loc_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_description);

        mineLocation = mineLocation.getInstance();

        Log.d(TAG, "lat, lng : " + mineLocation.getLatitude() + ", " + mineLocation.getLongitude());

        mInputName = (EditText) findViewById(R.id.input_name);
        mInputDate = (EditText) findViewById(R.id.input_date);
        mInputTel = (EditText) findViewById(R.id.input_tel);
        mInputDes = (EditText) findViewById(R.id.input_des);
        mButtonSave = (Button) findViewById(R.id.btn_save);

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToDatabase();
            }
        });

    }

    public void sendToDatabase() {

        SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault());
        Date date = new Date();

        id_user = MinemapPreference.getStoredSearchKey(getApplicationContext());
        loc_name = mInputName.getText().toString();
        loc_date = DateFormat.format(date);
        loc_tel = mInputTel.getText().toString();
        loc_des = mInputDes.getText().toString();

        new sendToBackground().execute(id_user, loc_name, loc_date, loc_tel, loc_des);
    }

    public class sendToBackground extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            id_user = strings[0];
            loc_name = strings[1];
            loc_date = strings[2];
            loc_tel = strings[3];
            loc_des = strings[4];
            loc_lat = String.valueOf(mineLocation.getLatitude());
            loc_long = String.valueOf(mineLocation.getLongitude());
            loc_type = "";
            loc_pic = "";

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
