package com.augmentis.ayp.minemap;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.augmentis.ayp.minemap.model.LocationItem;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapMainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final String TAG = "MapMainActivity";
    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    private ProgressDialog progress;
    private GoogleMap mGoogleMap;
    private UiSettings mUiSettings;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton fab;
    private boolean[] filter = new boolean[24];

    private LatLng latLng;
    private String mSearchKey;
    private String id_user;
    private String statusUrl;
    private ArrayList<String> list1;
    private LocationItem locationItem;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapMainActivity.this, MapRegisterActivity.class);
                startActivity(intent);
            }
        });

        sendToDatabase();

        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_main_fragment);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap = googleMap;
        mUiSettings = mGoogleMap.getUiSettings(); // set ui about map --> traffic building
        //set Map Type
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnMapClickListener(this);
        buildGoogleApiClient();

        mGoogleApiClient.connect();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            }
            return;
        }

        //current location button
        mGoogleMap.setMyLocationEnabled(true);
        //show traffic
        mGoogleMap.setTrafficEnabled(true);
        //show building
        mGoogleMap.setBuildingsEnabled(true);
        //compass
        mUiSettings.setCompassEnabled(true);
        //navigation option
        mUiSettings.setMapToolbarEnabled(false);

        setDefaultFilter();

        Log.d(TAG, "Size of Location Item: " + LocationItem.locationItems.size());
        if (LocationItem.locationItems.size() != 0) {
            for (int i = 0; i < LocationItem.locationItems.size(); i++) {
                addMarkerToGoogleMap(Integer.parseInt(LocationItem.locationItems.get(i).getLoc_type()),
                        Double.parseDouble(LocationItem.locationItems.get(i).getLoc_lat()),
                        Double.parseDouble(LocationItem.locationItems.get(i).getLoc_long()));
                Log.d(TAG, "Add Marker");
            }
        }
        progress.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Intent i = new Intent(this, MapMainActivity.class);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(i);
            }
        }
    }

    //new googleApiClient open app and go to current location
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        //get current location
        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(15).build();

        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_item, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQuery(mSearchKey, false);//
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submitted: " + query);
                mSearchKey = query;
                onSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changing: " + newText);
                return false;
            }


        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey, false);
            }
        });
        return true;
    }

    private void setDefaultFilter() {
        for (int i = 0; i < 24; i++) {
            filter[i] = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                filterMapTypeDialog();
                return true;
            case R.id.menu_map_type:
                showMapTypeSelectorDialog();
                return true;
            case R.id.menu_logout:
                Intent i = new Intent(MapMainActivity.this, LoginActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    private void filterMapTypeDialog() {

        CheckBox c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12,
                c13, c14, c15, c16, c17, c18, c19, c20, c21, c22, c23, c24;
        TextView okTextView;

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.marker_filter_dialog);

        okTextView = (TextView) dialog.findViewById(R.id.text_ok);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        c1 = (CheckBox) dialog.findViewById(R.id.checkbox_1);
        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[0] = !filter[0];
            }
        });

        c2 = (CheckBox) dialog.findViewById(R.id.checkbox_2);
        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[1] = true;
            }
        });

        c3 = (CheckBox) dialog.findViewById(R.id.checkbox_3);
        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[2] = true;
            }
        });

        c4 = (CheckBox) dialog.findViewById(R.id.checkbox_4);
        c4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[3] = true;
            }
        });

        c5 = (CheckBox) dialog.findViewById(R.id.checkbox_5);
        c5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[4] = true;
            }
        });

        c6 = (CheckBox) dialog.findViewById(R.id.checkbox_6);
        c6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[5] = true;
            }
        });

        c7 = (CheckBox) dialog.findViewById(R.id.checkbox_7);
        c7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[6] = true;
            }
        });

        c8 = (CheckBox) dialog.findViewById(R.id.checkbox_8);
        c8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[7] = true;
            }
        });

        c9 = (CheckBox) dialog.findViewById(R.id.checkbox_9);
        c9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[8] = true;
            }
        });

        c10 = (CheckBox) dialog.findViewById(R.id.checkbox_10);
        c10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[9] = true;
            }
        });

        c11 = (CheckBox) dialog.findViewById(R.id.checkbox_11);
        c11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[10] = true;
            }
        });

        c12 = (CheckBox) dialog.findViewById(R.id.checkbox_12);
        c12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[11] = true;
            }
        });

        c13 = (CheckBox) dialog.findViewById(R.id.checkbox_13);
        c13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[12] = true;
            }
        });

        c14 = (CheckBox) dialog.findViewById(R.id.checkbox_14);
        c14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[13] = true;
            }
        });

        c15 = (CheckBox) dialog.findViewById(R.id.checkbox_15);
        c15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[14] = true;
            }
        });

        c16 = (CheckBox) dialog.findViewById(R.id.checkbox_16);
        c16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[15] = true;
            }
        });

        c17 = (CheckBox) dialog.findViewById(R.id.checkbox_17);
        c17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[16] = true;
            }
        });

        c18 = (CheckBox) dialog.findViewById(R.id.checkbox_18);
        c18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[17] = true;
            }
        });

        c19 = (CheckBox) dialog.findViewById(R.id.checkbox_19);
        c19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[18] = true;
            }
        });

        c20 = (CheckBox) dialog.findViewById(R.id.checkbox_20);
        c20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[19] = true;
            }
        });

        c21 = (CheckBox) dialog.findViewById(R.id.checkbox_21);
        c21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[20] = true;
            }
        });

        c22 = (CheckBox) dialog.findViewById(R.id.checkbox_22);
        c22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[21] = true;
            }
        });

        c23 = (CheckBox) dialog.findViewById(R.id.checkbox_23);
        c23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[22] = true;
            }
        });

        c24 = (CheckBox) dialog.findViewById(R.id.checkbox_24);
        c24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[23] = true;
            }
        });

        dialog.show();
    }

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mGoogleMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    @Override
    public void onBackPressed() {
    }

    public void onSearch() {

        List<Address> addressList = null;

        if (mSearchKey != null || !mSearchKey.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(mSearchKey, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(16).build();

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void addMarkerToGoogleMap(int type, double lat, double lng) {

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(lat, lng));
        options.draggable(true).visible(true);

        switch (type) {
            case 1:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_airplane));
                break;
            case 2:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bank));
                break;
            case 3:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car));
                break;
            case 4:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_park));
                break;
            case 5:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cinema));
                break;
            case 6:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_coffee));
                break;
            case 7:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dessert));
                break;
            case 8:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fitness));
                break;
            case 9:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gas_station));
                break;
            case 10:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_goverment));
                break;
            case 11:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_home));
                break;
            case 12:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));
                break;
            case 13:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hotel));
                break;
            case 14:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mall));
                break;
            case 15:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mountain));
                break;
            case 16:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_other));
                break;
            case 17:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_park));
                break;
            case 18:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant));
                break;
            case 19:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_school));
                break;
            case 20:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sea));
                break;
            case 21:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ship));
                break;
            case 22:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_temple));
                break;
            case 23:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_train));
                break;
            case 24:
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_work));
                break;
        }
        mGoogleMap.addMarker(options);

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Map Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void sendToDatabase() {

        id_user = MinemapPreference.getStoredSearchKey(getApplicationContext());

        new sendToBackground().execute(id_user);
    }

    public class sendToBackground extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            id_user = strings[0];

            String url = "http://minemap.hol.es/find_location_main.php?id_user=" + id_user;


            JsonHttp jsonHttp = new JsonHttp();
            String strJson = null;

            try {
                strJson = jsonHttp.getJSONUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                JSONObject json = new JSONObject(strJson);
                String success = json.getString("status");

                if (success.equals("OK") == true) {
                    statusUrl = "OK";

                    JSONArray Json_array_size = json.getJSONArray("result");
                    for (int i = 0; i < Json_array_size.length(); i++) {
                        JSONObject object = Json_array_size.getJSONObject(i);

                        locationItem = new LocationItem();
                        locationItem.setLoc_id(object.getString("loc_id"));
                        locationItem.setId_user(object.getString("id_user"));
                        locationItem.setLoc_name(object.getString("loc_name"));
                        locationItem.setLoc_lat(object.getString("loc_lat"));
                        locationItem.setLoc_long(object.getString("loc_long"));
                        locationItem.setLoc_type(object.getString("loc_type"));
                        locationItem.setLoc_tel(object.getString("loc_tel"));
                        locationItem.setLoc_des(object.getString("loc_des"));
                        locationItem.setLoc_pic(object.getString("loc_pic"));
                        locationItem.setLoc_date(object.getString("loc_date"));
                        LocationItem.locationItems.add(locationItem);

                    }
                } else {
                    if (success.equals("NODATA") == true) {
                        statusUrl = "NODATA";
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return statusUrl;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "status --> " + LocationItem.locationItems.get(0).getLoc_name());
        }
    }
}
