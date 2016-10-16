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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private FloatingActionButton fab;

    private LatLng latLng;
    private TextView mTapTextView;
    private Marker mMarker;
    private Button mButton;

    private double lat, lng;
    private String mSearchKey;
    private int markerType = 1;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_register);

        fab = (FloatingActionButton) findViewById(R.id.fab_marker);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMarkerSelectorDialog();
            }
        });

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

        lat = latLng.latitude;
        lng = latLng.longitude;

        addMerkerToGoogleMap(lat, lng);
    }

    private void addMerkerToGoogleMap(double lat, double lng) {

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);

        switch (markerType) {
            case 1:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_airplane);
                break;
            case 2:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_bank);
                break;
            case 3:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_car);
                break;
            case 4:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_car_park);
                break;
            case 5:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_cinema);
                break;
            case 6:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_coffee);
                break;
            case 7:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_dessert);
                break;
            case 8:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_fitness);
                break;
            case 9:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_gas_station);
                break;
            case 10:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_goverment);
                break;
            case 11:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_home);
                break;
            case 12:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital);
                break;
            case 13:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_hotel);
                break;
            case 14:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_mall);
                break;
            case 15:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_mountain);
                break;
            case 16:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_other);
                break;
            case 17:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_park);
                break;
            case 18:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant);
                break;
            case 19:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_school);
                break;
            case 20:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_sea);
                break;
            case 21:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_ship);
                break;
            case 22:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_temple);
                break;
            case 23:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_train);
                break;
            case 24:
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_work);
                break;
        }

        if (mMarker != null) {
            mMarker.remove();
        }
        //add marker on same position of tapped
        mMarker = mGoogleMap.addMarker(new MarkerOptions()
                //marker option
                .icon(bitmapDescriptor)
                .position(new LatLng(lat, lng))
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

        LinearLayout l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12,
                l13, l14, l15, l16, l17, l18, l19, l20, l21, l22, l23, l24;

        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.marker_picker_dialog);
        dialog.setTitle(R.string.select_marker_please);

        l1 = (LinearLayout) dialog.findViewById(R.id.marker_1);
        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 1;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l2 = (LinearLayout) dialog.findViewById(R.id.marker_2);
        l2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 2;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l3 = (LinearLayout) dialog.findViewById(R.id.marker_3);
        l3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 3;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l4 = (LinearLayout) dialog.findViewById(R.id.marker_4);
        l4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 4;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l5 = (LinearLayout) dialog.findViewById(R.id.marker_5);
        l5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 5;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l6 = (LinearLayout) dialog.findViewById(R.id.marker_6);
        l6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 6;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l7 = (LinearLayout) dialog.findViewById(R.id.marker_7);
        l7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 7;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l8 = (LinearLayout) dialog.findViewById(R.id.marker_8);
        l8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 8;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l9 = (LinearLayout) dialog.findViewById(R.id.marker_9);
        l9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 9;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l10 = (LinearLayout) dialog.findViewById(R.id.marker_10);
        l10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 10;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l11 = (LinearLayout) dialog.findViewById(R.id.marker_11);
        l11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 11;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l12 = (LinearLayout) dialog.findViewById(R.id.marker_12);
        l12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 12;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l13 = (LinearLayout) dialog.findViewById(R.id.marker_13);
        l13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 13;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l14 = (LinearLayout) dialog.findViewById(R.id.marker_14);
        l14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 14;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l15 = (LinearLayout) dialog.findViewById(R.id.marker_15);
        l15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 15;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l16 = (LinearLayout) dialog.findViewById(R.id.marker_16);
        l16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 16;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l17 = (LinearLayout) dialog.findViewById(R.id.marker_17);
        l17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 17;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l18 = (LinearLayout) dialog.findViewById(R.id.marker_18);
        l18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 18;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l19 = (LinearLayout) dialog.findViewById(R.id.marker_19);
        l19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 19;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l20 = (LinearLayout) dialog.findViewById(R.id.marker_20);
        l20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 20;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l21 = (LinearLayout) dialog.findViewById(R.id.marker_21);
        l21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 21;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l22 = (LinearLayout) dialog.findViewById(R.id.marker_22);
        l22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 22;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l23 = (LinearLayout) dialog.findViewById(R.id.marker_23);
        l23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 23;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
        l24 = (LinearLayout) dialog.findViewById(R.id.marker_24);
        l24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerType = 24;
                dialog.dismiss();
                addMerkerToGoogleMap(lat, lng);
            }
        });
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

        mineLocation.setLatitude(lat);
        mineLocation.setLongitude(lng);
        mineLocation.setType(markerType);

        Intent intent = new Intent(MapRegisterActivity.this, LocationDescription.class);
        startActivity(intent);
    }
}