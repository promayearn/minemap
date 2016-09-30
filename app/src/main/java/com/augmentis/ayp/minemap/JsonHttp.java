package com.augmentis.ayp.minemap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Apinya on 9/26/2016.
 */

public class JsonHttp {

    private static String TAG = "JsonHttp";


    public String getJSONUrl(String url) throws IOException {

        URL urlTest = new URL(url);

        HttpURLConnection connection = (HttpURLConnection) urlTest.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            //read data from Stream
            InputStream in = connection.getInputStream();

            //if connection is not OK throw new IOException
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + url);
            }

            int bytesRead = 0;

            byte[] buffer = new byte[2048];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();

            return out.toString();

        } finally {
            connection.disconnect();
        }
    }


}
