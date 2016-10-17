package com.augmentis.ayp.minemap;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.augmentis.ayp.minemap.model.LocationItem;
import com.augmentis.ayp.minemap.model.MineLocation;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationDescription extends AppCompatActivity {

    private static final int REQUEST_CAPTURE_PHOTO = 2;
    private static String TAG = "LocationDescription";

    private EditText mInputName;
    private EditText mInputTel;
    private EditText mInputDes;
    private EditText mInputOpen;
    private EditText mInputClose;


    private MineLocation mineLocation;

    private String loc_name;
    private String loc_date;
    private String loc_tel;
    private String loc_des;
    protected String loc_lat;
    protected String loc_long;
    protected String loc_open;
    protected String loc_close;
    private String id_user;
    protected String loc_type;
    protected String statusUrl;


    public Uri uri;
    public String image_str;

    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_description);

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
            public void onClick(View view) {
                sendToDatabase();
                Intent i = new Intent(LocationDescription.this, MapMainActivity.class);
                startActivity(i);
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
        loc_open = mInputOpen.getText().toString();
        loc_close = mInputClose.getText().toString();

        new sendToBackground().execute(id_user, loc_name, loc_date, loc_tel, loc_des, loc_open, loc_close);
    }

    public class sendToBackground extends AsyncTask<String, String, String> {

        ProgressDialog loading;

        @Override
        protected String doInBackground(String... strings) {

            id_user = strings[0];
            loc_name = strings[1];
            loc_date = strings[2];
            loc_tel = strings[3];
            loc_des = strings[4];
            loc_open = strings[5];
            loc_close = strings[6];
            loc_lat = String.valueOf(mineLocation.getLatitude());
            loc_long = String.valueOf(mineLocation.getLongitude());
            loc_type = String.valueOf(mineLocation.getType());


            String strURL = "http://minemap.hol.es/add_location.php?id_user=" + id_user + "&loc_name=" + loc_name +
                    "&loc_lat=" + loc_lat + "&loc_long=" + loc_long + "&loc_type=" + loc_type + "&loc_tel=" + loc_tel +
                    "&loc_des=" + loc_des + "&loc_date=" + loc_date + "&loc_open=" + loc_open + "&loc_close=" + loc_close;

            JsonHttp jsonHttp = new JsonHttp();
            String strJson = null;

            try {

                strJson = jsonHttp.getJSONUrl(strURL);

            } catch (IOException e) {
                e.printStackTrace();
            }

            statusUrl = strJson;

            return statusUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(LocationDescription.this, "Save New Location", "Please wait...", true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(getApplicationContext(), "Save Success", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LocationDescription.this, MapMainActivity.class);
            startActivity(intent);

        }
    }

}
