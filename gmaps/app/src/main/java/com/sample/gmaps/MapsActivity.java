package com.sample.gmaps;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    GoogleMap gMap;
    GoogleMap.OnCameraIdleListener onCameraIdleListener;
    SupportMapFragment mapFragment;
    LocationManager locationManager;
    LocationListener locationListener;
    double latitude = 0;
    double longitude = 0;
    private static final int PERMISSION_REQUEST_CODE = 100;
    Button saveLocationButton;
    LocationDBHelper db;
    TextView resultText;
    Location location;
    // flag for GPS status
    public boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute
    LatLng currentLocation;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(12.9220555, 77.6812774);
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    public Criteria criteria;
    public String bestProvider;
    Dialog dialogLocationName;
    double defaultBangaloreLatitude = 12.9716;
    double defaultBangaloreLongitude = 77.5946;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }*/
        setContentView(R.layout.activity_maps);
        //location = new Location("Location");
        //location = getLocation();
        //latitude = location.getLatitude();
        //longitude = location.getLongitude();
        //currentLocation = new LatLng(latitude, longitude);

        /*mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);*/

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //resultText = (TextView) findViewById(R.id.dragg_result);
        //configureCameraIdle();

        db = LocationDBHelper.getInstance(this);
        saveLocationButton = (Button) findViewById(R.id.save_location);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationName = "Home";
                String locationCategory = "Home";
                saveLocationNameDetails(view);
                saveLocationToDB(locationName, locationCategory, latitude, longitude);
                saveLocationToDB("Office", "Office", 12.9220555, 77.6812774);
            }
        });
    }

    public void saveLocationNameDetails(View view){
        dialogLocationName = new Dialog(this);
        dialogLocationName.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLocationName.setContentView(R.layout.dialog_location_name);
        dialogLocationName.show();
        dialogLocationName.setCancelable(false);
        Button btSave = (Button)dialogLocationName.findViewById(R.id.btSave);
        Button btCancel = (Button)dialogLocationName.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLocationName.dismiss();
                dialogLocationName.cancel();
            }
        });
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Input Validations
            }
        });
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        if (gMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, gMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }*/

    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            //showCurrentPlace();
        }
        return true;
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (gMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();
                            } else {
                                //Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            //Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            gMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                gMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    private void configureCameraIdle() {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng latLng = gMap.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(MapsActivity.this);

                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty() && !country.isEmpty())
                            resultText.setText(locality + "  " + country);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, INTERNET, ACCESS_NETWORK_STATE}, PERMISSION_REQUEST_CODE);
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                break;
        }
    }*/

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
    public void onMapReady(final GoogleMap googleMap) {
        gMap = googleMap;
        /*gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();*/

        /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/

        if (!checkPermission()) {
            requestPermission();
        }
        gMap.setMyLocationEnabled(true);
        Location myLocation = getCurrentLocation();
        setDefaultLocation(myLocation);
        /*try{
            LatLng myLocationLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            gMap.addMarker(new MarkerOptions().position(myLocationLatLng).title("Your Current Location").draggable(true));
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 14.0f));
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Unable to get Current Location.\nSetting up default location.", Toast.LENGTH_SHORT).show();
            LatLng currentLocationBangalore = new LatLng(defaultBangaloreLatitude, defaultBangaloreLongitude);
            gMap.addMarker(new MarkerOptions().position(currentLocationBangalore).title("Your Current Location").draggable(true));
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultBangaloreLatitude, defaultBangaloreLongitude), 14.0f));
        }*/

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                longitude = marker.getPosition().longitude;
                latitude = marker.getPosition().latitude;
                Toast.makeText(MapsActivity.this, "New location: "+marker.getPosition(), Toast.LENGTH_SHORT).show();
            }
        });
        //mMap.setOnCameraIdleListener(onCameraIdleListener);
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        gMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").draggable(true));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    public void setDefaultLocation(Location myLocation) {
        try {
            if ((myLocation == null) || (myLocation.getLatitude() == 0 && myLocation.getLongitude() == 0)) {
                Toast.makeText(this, "Unable to get Current Location.\nSetting up default location to Bangalore.", Toast.LENGTH_SHORT).show();
                LatLng currentLocationBangalore = new LatLng(defaultBangaloreLatitude, defaultBangaloreLongitude);
                gMap.addMarker(new MarkerOptions().position(currentLocationBangalore).title("Bangalore").draggable(true));
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultBangaloreLatitude, defaultBangaloreLongitude), 14.0f));
            } else {
                LatLng myLocationLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                gMap.addMarker(new MarkerOptions().position(myLocationLatLng).title("Your Current Location").draggable(true));
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 14.0f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }*/

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (gMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                gMap.setMyLocationEnabled(true);
                gMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                gMap.setMyLocationEnabled(false);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            //Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null)
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            /*Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());*/
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            gMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            //Log.e("Exception: %s", e.getMessage());
        }
    }


    public Location getCurrentLocation() {
        Location currentLocation = new Location("Current Location");
        try {
            if (!checkPermission()) {
                requestPermission();
            } /*else {
                //Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();
                //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }*/

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isNetworkEnabled){
                Toast.makeText(this, "GPS is not Enabled.\nKindly enable GPS on device.", Toast.LENGTH_SHORT).show();
                //Return from app
            }

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    currentLocation = null;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (currentLocation != null) {
                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    currentLocation = null;
                    if (currentLocation == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (locationManager != null) {
                            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            //currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                            if (currentLocation != null) {
                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentLocation;
    }

    public void saveLocationToDB(String locationName, String locationCategory, double locationLatitude, double locationLongitude) {
        String query_logs = "INSERT INTO " + Constants.LOCATION_TABLE_NAME + "(" + LocationDBColNames.NAME + "," + LocationDBColNames.CATEGORY
                + "," + LocationDBColNames.LATITUDE + "," + LocationDBColNames.LONGITUDE
                + ") VALUES (" + "'" + locationName + "'" + ", '" + locationCategory + "'" + ", '"
                + locationLatitude + "'" + ", '" + locationLongitude + "'" + ")";
        db.executeInsertQuery(query_logs);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}