package com.augmentis.ayp.minemap;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.augmentis.ayp.minemap.model.MineLocation;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapRegisterActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static String TAG = "MapRegisterActivity";
    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    private GoogleMap mGoogleMap;
    private UiSettings mUiSettings;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private LatLng latLng;
    private TextView mTapTextView;
    private Marker mMarker;
    private Button mButton;
    private String mSearchKey;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_register);

        mTapTextView = (TextView) findViewById(R.id.lat_lng_on_touch);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_register_fragment);

        mapFragment.getMapAsync(this);

        mButton = (Button) findViewById(R.id.next_btn);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        mUiSettings = mGoogleMap.getUiSettings();
        //set Map Type
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnMapClickListener(this);
        buildGoogleApiClient();

        mGoogleApiClient.connect();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
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

        mUiSettings.setMapToolbarEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(getIntent());
                finish();
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

    //connect and get current location
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
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

    //get LatLng on tapped
    @Override
    public void onMapClick(LatLng latLng) {
        mTapTextView.setText("Tapped, Point: " + latLng);
        if (mMarker != null) {
            mMarker.remove();
        }
        //add marker on same position of tapped
        mMarker = mGoogleMap.addMarker(new MarkerOptions()
                //marker option
                .title("Here?")
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .draggable(true).visible(true));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register_item, menu);
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
            case android.R.id.home:
                onBackPressed();
                Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_marker_type:
                showMarkerSelectorDialog();
                break;
            case R.id.menu_map_type:
                showMapTypeSelectorDialog();
                break;
            case R.id.menu_logout:
                Intent i = new Intent(MapRegisterActivity.this, LoginActivity.class);
                startActivity(i);
                break;
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

    private void showMarkerSelectorDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.marker_picker_dialog);
        dialog.setTitle("Select Marker Type");

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    @Override
    public void onClick(View v) {

        MineLocation mineLocation = MineLocation.getInstance();

        mineLocation.setLatitude(latLng.latitude);
        mineLocation.setLongitude(latLng.longitude);

        Intent intent = new Intent(MapRegisterActivity.this, LocationDescription.class);
        startActivity(intent);
    }
}