package com.augmentis.ayp.minemap;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.augmentis.ayp.minemap.model.RequestHandler;
import com.bumptech.glide.Glide;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class ViewImage extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ViewImage";

    private EditText editTextId;
    protected Button buttonGetImage;
    private ImageView imageView;

    protected RequestHandler requestHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        editTextId = (EditText) findViewById(R.id.editTextId);
        buttonGetImage = (Button) findViewById(R.id.buttonGetImage);
        imageView = (ImageView) findViewById(R.id.imageViewShow);

        requestHandler = new RequestHandler();

        buttonGetImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        getImage();
    }

    private void getImage() {

        String id = editTextId.getText().toString().trim();

        class GetImage extends AsyncTask<String,Void,Bitmap> {

            private ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ViewImage.this, "Uploading...", null,true,true);
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                String id = params[0];
                String add = "http://minemap.hol.es/getImage64.php?id="+id;

                URL url = null;
                Bitmap image = null;

                try {
//                    url = new URL(add);

                    InputStream in = new URL(add).openStream();
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();

                    byte[] buff = new byte[1024];

                    StringBuilder sb = new StringBuilder();

                    while(in.read(buff, 0, 1024) > 0) {
                        sb.append(new String(buff));
                    }
                    Log.d(TAG, " data = " + sb.toString());
                    Log.d(TAG, " u r l " + in);

                    byte[] imgBytes = Base64.decode(sb.toString(), Base64.NO_WRAP);


                    image = BitmapFactory.decodeByteArray( imgBytes, 0, imgBytes.length );
                    Log.d(TAG, " i m a g e " + image);

                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                return image;
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                loading.dismiss();
//                Glide.with(getApplicationContext()).load(b).into(imageView);
                imageView.setImageBitmap(b);
            }
        }

        GetImage gi = new GetImage();
        gi.execute(id);
    }
}
