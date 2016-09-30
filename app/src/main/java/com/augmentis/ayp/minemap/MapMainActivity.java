package com.augmentis.ayp.minemap;

import android.Manifest;
import android.content.DialogInterface;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapMainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final String TAG = "MapMainActivity";
    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    private GoogleMap mGoogleMap;
    private UiSettings mUiSettings;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton fab;

    private LatLng latLng;
    private String mSearchKey;
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

        mGoogleMap.addMarker(addMarkerToGoogleMap(24, 13.721256, 100.530055));
        mGoogleMap.addMarker(addMarkerToGoogleMap(17, 13.729769, 100.537156));
        mGoogleMap.addMarker(addMarkerToGoogleMap(1, 13.688138, 100.749261));
        mGoogleMap.addMarker(addMarkerToGoogleMap(11, 13.765903, 100.538376));
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

        Toast.makeText(this, "MineLocation Updated", Toast.LENGTH_SHORT).show();

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
        switch (view.getId()) {
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
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

    public MarkerOptions addMarkerToGoogleMap(int type, double lat, double lng) {

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

        return options;
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
