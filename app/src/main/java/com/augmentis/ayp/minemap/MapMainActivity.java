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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private ProgressDialog progress;
    private GoogleMap mGoogleMap;
    private UiSettings mUiSettings;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton mFloatButton;
    private boolean[] filter = new boolean[24];

    private LatLng latLng;
    private String mSearchKey;
    private String id_user;
    private String statusUrl;
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

        mFloatButton = (FloatingActionButton) findViewById(R.id.fab);

        mFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapMainActivity.this, MapRegisterActivity.class);
                startActivity(intent);
            }
        });

        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();

        sendToDatabase();

        progress.dismiss();

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

        mGoogleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });

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

        addMarker();
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
            case R.id.menu_reload:
                addMarker();
                return true;
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

    private void addMarker() {

        if (LocationItem.locationItems.size() != 0) {
            for (int i = 0; i < LocationItem.locationItems.size(); i++) {
                addMarkerToGoogleMap(Integer.parseInt(LocationItem.locationItems.get(i).getLoc_type()),
                        Double.parseDouble(LocationItem.locationItems.get(i).getLoc_lat()),
                        Double.parseDouble(LocationItem.locationItems.get(i).getLoc_long()),
                        LocationItem.locationItems.get(i).getLoc_name() + "," + LocationItem.locationItems.get(i).getLoc_type()
                                + "," + LocationItem.locationItems.get(i).getLoc_tel() + "," + LocationItem.locationItems.get(i).getLoc_des()
                                + "," + LocationItem.locationItems.get(i).getLoc_date() + "," + LocationItem.locationItems.get(i).getLoc_open()
                                + "," + LocationItem.locationItems.get(i).getLoc_close());
                Log.d(TAG, "Add Marker");
            }
        }
    }

    private void filterMapTypeDialog() {

        CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7, checkBox8,
                checkBox9, checkBox10, checkBox11, checkBox12, checkBox13, checkBox14, checkBox15, checkBox16,
                checkBox17, checkBox18, checkBox19, checkBox20, checkBox21, checkBox22, checkBox23, checkBox24;
        TextView okTextView;

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.marker_filter_dialog);
        dialog.setTitle(R.string.select_filter_please);

        okTextView = (TextView) dialog.findViewById(R.id.text_ok);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mGoogleMap.clear();
                addMarker();
            }
        });

        checkBox1 = (CheckBox) dialog.findViewById(R.id.checkbox_1);
        if (filter[0]) {
            checkBox1.setChecked(filter[0]);
        }
        checkBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[0] = !filter[0];
            }
        });

        checkBox2 = (CheckBox) dialog.findViewById(R.id.checkbox_2);
        if (filter[1]) {
            checkBox2.setChecked(filter[1]);
        }
        checkBox2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[1] = !filter[1];
            }
        });

        checkBox3 = (CheckBox) dialog.findViewById(R.id.checkbox_3);
        if (filter[2]) {
            checkBox3.setChecked(filter[2]);
        }
        checkBox3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[2] = !filter[2];
            }
        });

        checkBox4 = (CheckBox) dialog.findViewById(R.id.checkbox_4);
        if (filter[3]) {
            checkBox4.setChecked(filter[3]);
        }
        checkBox4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[3] = !filter[3];
            }
        });

        checkBox5 = (CheckBox) dialog.findViewById(R.id.checkbox_5);
        if (filter[4]) {
            checkBox5.setChecked(filter[4]);
        }
        checkBox5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[4] = !filter[4];
            }
        });

        checkBox6 = (CheckBox) dialog.findViewById(R.id.checkbox_6);
        if (filter[5]) {
            checkBox6.setChecked(filter[5]);
        }
        checkBox6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[5] = !filter[5];
            }
        });

        checkBox7 = (CheckBox) dialog.findViewById(R.id.checkbox_7);
        if (filter[6]) {
            checkBox7.setChecked(filter[6]);
        }
        checkBox7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[6] = !filter[6];
            }
        });

        checkBox8 = (CheckBox) dialog.findViewById(R.id.checkbox_8);
        if (filter[7]) {
            checkBox8.setChecked(filter[7]);
        }
        checkBox8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[7] = !filter[7];
            }
        });

        checkBox9 = (CheckBox) dialog.findViewById(R.id.checkbox_9);
        if (filter[8]) {
            checkBox9.setChecked(filter[8]);
        }
        checkBox9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[8] = !filter[8];
            }
        });

        checkBox10 = (CheckBox) dialog.findViewById(R.id.checkbox_10);
        if (filter[9]) {
            checkBox10.setChecked(filter[9]);
        }
        checkBox10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[9] = !filter[9];
            }
        });

        checkBox11 = (CheckBox) dialog.findViewById(R.id.checkbox_11);
        if (filter[10]) {
            checkBox11.setChecked(filter[10]);
        }
        checkBox11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[10] = !filter[10];
            }
        });

        checkBox12 = (CheckBox) dialog.findViewById(R.id.checkbox_12);
        if (filter[11]) {
            checkBox12.setChecked(filter[11]);
        }
        checkBox12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[11] = !filter[11];
            }
        });

        checkBox13 = (CheckBox) dialog.findViewById(R.id.checkbox_13);
        if (filter[12]) {
            checkBox13.setChecked(filter[12]);
        }
        checkBox13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[12] = !filter[12];
            }
        });

        checkBox14 = (CheckBox) dialog.findViewById(R.id.checkbox_14);
        if (filter[13]) {
            checkBox14.setChecked(filter[13]);
        }
        checkBox14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[13] = !filter[13];
            }
        });

        checkBox15 = (CheckBox) dialog.findViewById(R.id.checkbox_15);
        if (filter[14]) {
            checkBox15.setChecked(filter[14]);
        }
        checkBox15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[14] = !filter[14];
            }
        });

        checkBox16 = (CheckBox) dialog.findViewById(R.id.checkbox_16);
        if (filter[15]) {
            checkBox16.setChecked(filter[15]);
        }
        checkBox16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[15] = !filter[15];
            }
        });

        checkBox17 = (CheckBox) dialog.findViewById(R.id.checkbox_17);
        if (filter[16]) {
            checkBox17.setChecked(filter[16]);
        }
        checkBox17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[16] = !filter[16];
            }
        });

        checkBox18 = (CheckBox) dialog.findViewById(R.id.checkbox_18);
        if (filter[17]) {
            checkBox18.setChecked(filter[17]);
        }
        checkBox18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[17] = !filter[17];
            }
        });

        checkBox19 = (CheckBox) dialog.findViewById(R.id.checkbox_19);
        if (filter[18]) {
            checkBox19.setChecked(filter[18]);
        }
        checkBox19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[18] = !filter[18];
            }
        });

        checkBox20 = (CheckBox) dialog.findViewById(R.id.checkbox_20);
        if (filter[19]) {
            checkBox20.setChecked(filter[19]);
        }
        checkBox20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[19] = !filter[19];
            }
        });

        checkBox21 = (CheckBox) dialog.findViewById(R.id.checkbox_21);
        if (filter[20]) {
            checkBox21.setChecked(filter[20]);
        }
        checkBox21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[20] = !filter[20];
            }
        });

        checkBox22 = (CheckBox) dialog.findViewById(R.id.checkbox_22);
        if (filter[21]) {
            checkBox22.setChecked(filter[21]);
        }
        checkBox22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[21] = !filter[21];
            }
        });

        checkBox23 = (CheckBox) dialog.findViewById(R.id.checkbox_23);
        if (filter[22]) {
            checkBox23.setChecked(filter[22]);
        }
        checkBox23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[22] = !filter[22];
            }
        });

        checkBox24 = (CheckBox) dialog.findViewById(R.id.checkbox_24);
        if (filter[23]) {
            checkBox24.setChecked(filter[23]);
        }
        checkBox24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter[23] = !filter[23];
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

    public void addMarkerToGoogleMap(int type, double lat, double lng, String title) {

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(lat, lng));
        options.title(title);
        options.draggable(true).visible(true);

        if (type == 1 && !filter[0]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_airplane));
            mGoogleMap.addMarker(options);
        } else if (type == 2 && !filter[1]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bank));
            mGoogleMap.addMarker(options);
        } else if (type == 3 && !filter[2]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car));
            mGoogleMap.addMarker(options);
        } else if (type == 4 && !filter[3]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_park));
            mGoogleMap.addMarker(options);
        } else if (type == 5 && !filter[4]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cinema));
            mGoogleMap.addMarker(options);
        } else if (type == 6 && !filter[5]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_coffee));
            mGoogleMap.addMarker(options);
        } else if (type == 7 && !filter[6]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dessert));
            mGoogleMap.addMarker(options);
        } else if (type == 8 && !filter[7]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fitness));
            mGoogleMap.addMarker(options);
        } else if (type == 9 && !filter[8]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gas_station));
            mGoogleMap.addMarker(options);
        } else if (type == 10 && !filter[9]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_goverment));
            mGoogleMap.addMarker(options);
        } else if (type == 11 && !filter[10]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_home));
            mGoogleMap.addMarker(options);
        } else if (type == 12 && !filter[11]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));
            mGoogleMap.addMarker(options);
        } else if (type == 13 && !filter[12]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hotel));
            mGoogleMap.addMarker(options);
        } else if (type == 14 && !filter[13]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mall));
            mGoogleMap.addMarker(options);
        } else if (type == 15 && !filter[14]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mountain));
            mGoogleMap.addMarker(options);
        } else if (type == 16 && !filter[15]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_other));
            mGoogleMap.addMarker(options);
        } else if (type == 17 && !filter[16]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_park));
            mGoogleMap.addMarker(options);
        } else if (type == 18 && !filter[17]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant));
            mGoogleMap.addMarker(options);
        } else if (type == 19 && !filter[18]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_school));
            mGoogleMap.addMarker(options);
        } else if (type == 20 && !filter[19]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sea));
            mGoogleMap.addMarker(options);
        } else if (type == 21 && !filter[20]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ship));
            mGoogleMap.addMarker(options);
        } else if (type == 22 && !filter[21]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_temple));
            mGoogleMap.addMarker(options);
        } else if (type == 23 && !filter[22]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_train));
            mGoogleMap.addMarker(options);
        } else if (type == 24 && !filter[23]) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_work));
            mGoogleMap.addMarker(options);
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

    @Override
    protected void onResume() {
        super.onResume();
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

                if (success.equals("OK")) {
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
                        locationItem.setLoc_date(object.getString("loc_date"));
                        locationItem.setLoc_open(object.getString("loc_open"));
                        locationItem.setLoc_close(object.getString("loc_close"));
                        LocationItem.locationItems.add(locationItem);
                    }
                } else {
                    if (success.equals("NODATA")) {
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
        }
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_item, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            String[] data = marker.getTitle().split(",");
            String type = "";

            switch (data[1]) {
                case "1":
                    type = "Airport";
                    break;
                case "2":
                    type = "Bank";
                    break;
                case "3":
                    type = "Car Transportation";
                    break;
                case "4":
                    type = "Parking Lot";
                    break;
                case "5":
                    type = "Cinema";
                    break;
                case "6":
                    type = "Coffee Shop";
                    break;
                case "7":
                    type = "Dessert Cafe";
                    break;
                case "8":
                    type = "Fitness";
                    break;
                case "9":
                    type = "Gas Station";
                    break;
                case "10":
                    type = "Government Office";
                    break;
                case "11":
                    type = "Home";
                    break;
                case "12":
                    type = "Hospital";
                    break;
                case "13":
                    type = "Hotel";
                    break;
                case "14":
                    type = "Mall";
                    break;
                case "15":
                    type = "Mountain";
                    break;
                case "16":
                    type = "Other";
                    break;
                case "17":
                    type = "Park";
                    break;
                case "18":
                    type = "Restaurant";
                    break;
                case "19":
                    type = "School";
                    break;
                case "20":
                    type = "Sea";
                    break;
                case "21":
                    type = "Ship Transportation";
                    break;
                case "22":
                    type = "Temple";
                    break;
                case "23":
                    type = "Train Transportation";
                    break;
                case "24":
                    type = "Work Place";
                    break;
            }

            TextView textViewLocName = ((TextView) myContentsView.findViewById(R.id.tvLocName));
            textViewLocName.setText(data[0]);
            TextView textViewType = ((TextView) myContentsView.findViewById(R.id.tvType));
            textViewType.setText(type);
            TextView textViewTelephone = ((TextView) myContentsView.findViewById(R.id.tvTelephone));
            textViewTelephone.setText(data[2]);
            TextView textViewDescription = ((TextView) myContentsView.findViewById(R.id.tvDescription));
            textViewDescription.setText(data[3]);
            TextView textViewTimeCreate = ((TextView) myContentsView.findViewById(R.id.tvTimeCreate));
            textViewTimeCreate.setText(data[4]);
            TextView textViewTimeOpen = ((TextView) myContentsView.findViewById(R.id.tvOpen));
            textViewTimeOpen.setText(data[5]);
            TextView textViewTimeClose = ((TextView) myContentsView.findViewById(R.id.tvClose));
            textViewTimeClose.setText(data[6]);

            return myContentsView;
        }
    }
}
