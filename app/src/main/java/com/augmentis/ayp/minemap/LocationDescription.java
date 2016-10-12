package com.augmentis.ayp.minemap;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
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

    public LocationItem locationItem;
//    public Uri uri;
    public String imageFileName;
    public File filePhoto;
    public Uri uri;
    public String image_str;

    Bitmap bitmap = null;

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
        mImgView = (ImageView) findViewById(R.id.imgShow);

        mButtonSave = (Button) findViewById(R.id.btn_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToDatabase();
                Intent i = new Intent(LocationDescription.this, MapMainActivity.class);
                startActivity(i);
            }
        });

        mButtonCamera = (FloatingActionButton) findViewById(R.id.btn_camera);
        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                imageFileName = "IMG_" + timeStamp + ".jpg";
//                filePhoto = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera/" + imageFileName);
//
//                uri = Uri.fromFile(filePhoto);
//
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                    startActivityForResult(takePictureIntent, REQUEST_CAPTURE_PHOTO);
//
//
//                    Log.d(TAG, "T E S T F I L E -------------- > " + takePictureIntent);
//                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CAPTURE_PHOTO);

            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "R E S U L T CODE : " + data);

        if (requestCode == REQUEST_CAPTURE_PHOTO && resultCode == RESULT_OK) {
            uri = data.getData();

            Log.d(TAG, " path u r i = " + uri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mImgView.setImageBitmap(bitmap);
                imgToBase64(bitmap);
                Log.d(TAG, " b i t m a p --> " + bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

//            Bundle extras = data.getExtras();
//            Bitmap bitmap = (Bitmap) extras.get("data");
//            mImgView.setImageBitmap(bitmap);

//            String img = String.valueOf(bitmap);
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//            Bitmap bitmapOut = BitmapFactory.decodeFile(img);
//            bitmapOut.compress(Bitmap.CompressFormat.JPEG, 20, stream);
//            byte [] byte_arr = stream.toByteArray();
//            image_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);
//            Log.d(TAG, " b i t m a p : d e c o d e --> " + image_str);

        }
    }

    public void imgToBase64(Bitmap bm) throws IOException {
        ByteArrayOutputStream out = null;

            out = new ByteArrayOutputStream();
            // compress image
            bm.compress(Bitmap.CompressFormat.JPEG, 10, out);

            out.flush();
            out.close();

            byte[] imgBytes = out.toByteArray();
            image_str = Base64.encodeToString(imgBytes, Base64.DEFAULT);
//            Log.d(TAG, " s t r i n g -- > image_str : " + image_str);
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

            loc_pic = String.valueOf(uri);

            Log.d(TAG , " loc pic from uri =" +loc_pic);

            String strURL = "http://minemap.hol.es/add_location.php?id_user=" + id_user + "&loc_name=" + loc_name +
                    "&loc_lat=" + loc_lat + "&loc_long=" + loc_long + "&loc_type=" + loc_type + "&loc_tel=" + loc_tel +
                    "&loc_des=" + loc_des + "&loc_pic=" + loc_pic + "&loc_date=" + loc_date;

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(getApplicationContext(), "Save Success", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LocationDescription.this, MapMainActivity.class);
            startActivity(intent);

        }
    }

}
