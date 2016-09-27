package com.augmentis.ayp.minemap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final String TAG = "MapActivity";
    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private TextView mLatLngTextView;
    private LatLng latLng;
    private double lat;
    private double lng;
    private Marker marker;
    private String mSearchKey;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        mLatLngTextView = (TextView) findViewById(R.id.lat_lng_on_touch);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
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

        mMap = googleMap;

        mMap = googleMap;
        mUiSettings = mMap.getUiSettings(); // set ui about map --> traffic building

        //set Map Type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMapClickListener(this);
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
        mMap.setMyLocationEnabled(true);
        //show traffic
        mMap.setTrafficEnabled(true);
        //show building
        mMap.setBuildingsEnabled(true);
        //compass
        mUiSettings.setCompassEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Intent i = new Intent(this, MapActivity.class);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(i);
            }
        }
    }

    //new googleApiClient open app and go to current location
    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show();

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(15).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mLatLngTextView.setText("Tapped, Point : " + latLng);
        lat = latLng.latitude;
        lng = latLng.longitude;
        if (marker != null) {
            marker.remove();
        }
        //add marker on same position of tapped
        marker = mMap.addMarker(new MarkerOptions()
                //marker option
                .title("Place")
                .snippet("Here?")
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .draggable(true).visible(true));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

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
                    .target(latLng).zoom(18).build();

            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
}
