package com.augmentis.ayp.minemap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.augmentis.ayp.minemap.model.LocationItem;
import com.augmentis.ayp.minemap.model.MineLocation;
import com.augmentis.ayp.minemap.model.PictureUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationDescription extends AppCompatActivity {

    private static final int REQUEST_CAPTURE_PHOTO = 121;
    private static String TAG = "LocationDescription";

    private EditText mInputName;
    private EditText mInputTel;
    private EditText mInputDes;

    private Button mButtonSave;
    private FloatingActionButton mButtonCamera;
    private ImageView mImgView;

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
    private String statusUrl;

    private File photoFile;
    public LocationItem locationItem;
    public Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_description);

        mineLocation = MineLocation.getInstance();

        Log.d(TAG, "lat, lng : " + mineLocation.getLatitude() + ", " +
                mineLocation.getLongitude() + ", " + mineLocation.getType());

        mInputName = (EditText) findViewById(R.id.input_name);
        mInputTel = (EditText) findViewById(R.id.input_tel);
        mInputDes = (EditText) findViewById(R.id.input_des);
        mImgView = (ImageView) findViewById(R.id.imageView);

        mButtonSave = (Button) findViewById(R.id.btn_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToDatabase();
            }
        });

//        PackageManager packageManager = getApplicationContext().getPackageManager();
//        //call camera intent
//        final Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        boolean canTakePhoto = photoFile != null && captureImageIntent.resolveActivity(packageManager) != null;
//
//        if(canTakePhoto){
//            Uri uri = Uri.fromFile(photoFile);
//
//            Log.d(TAG, "File output at" + photoFile.getAbsolutePath());
//            captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        }

        mButtonCamera = (FloatingActionButton) findViewById(R.id.btn_camera);
        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp =
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";
                File f = new File(Environment.getExternalStorageDirectory()
                        , "DCIM/Camera/" + imageFileName);
                uri = Uri.fromFile(f);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(Intent.createChooser(intent
                        , "Take a picture with"), REQUEST_CAPTURE_PHOTO);

                Log.d(TAG, "T E S T F I L E -------------- > " + imageFileName);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_PHOTO && resultCode == RESULT_OK) {
            getContentResolver().notifyChange(uri, null);
            ContentResolver cr = getContentResolver();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
                mImgView.setImageBitmap(bitmap);
                Toast.makeText(getApplicationContext()
                        , uri.getPath(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            loc_type = String.valueOf(mineLocation.getType());
            loc_pic = "test";

            String strURL = "http://minemap.hol.es/add_location.php?id_user=" + id_user + "&loc_name=" + loc_name +
                    "&loc_lat=" + loc_lat + "&loc_long=" + loc_long + "&loc_type=" + loc_type + "&loc_tel=" + loc_tel +
                    "&loc_des=" + loc_des + "&loc_pic=" + loc_pic + "&loc_date=" + loc_date;

            JsonHttp jsonHttp = new JsonHttp();
            String  strJson = null;

            try {

                strJson = jsonHttp.getJSONUrl(strURL);

            } catch (IOException e) {
                e.printStackTrace();
            }

            statusUrl = strJson;
            Log.e("Update : ", strJson);

            return statusUrl;
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
