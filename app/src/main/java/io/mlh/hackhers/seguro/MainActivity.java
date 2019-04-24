package io.mlh.hackhers.seguro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Geocoder mGcd;
    private AutocompleteSupportFragment autocompleteFragment;
    private double[] currLocation;
    private double[] destLocation;
    private LocationCallback mLocationCallback;


    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1011;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currLocation = new double[2];
        destLocation = new double[2];

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGcd = new Geocoder(getBaseContext(), Locale.getDefault());

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName() + ", " + place.getId());

                try {
                    Address address = mGcd.getFromLocationName(place.getName(), 1).get(0);
                    destLocation = new double[]{address.getLatitude(), address.getLongitude()};

                    // so there are no more than two markers on the map at any time
                    mMap.clear();
                    moveToLocation(currLocation, false);
                    moveToLocation(destLocation, false);

                    LatLngBounds diff = new LatLngBounds(new LatLng(currLocation[0], currLocation[1]), new LatLng(destLocation[0], destLocation[1]));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(diff, 0));

                    // calculate and show the route options for the trip
                    plotRoute();
                    Toast.makeText(MainActivity.this, "lets begin", Toast.LENGTH_SHORT).show();

                    //keepCurrentLocationUpdated();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                System.out.println("An error occurred: " + status);
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                System.out.println("locations");
                for (Location location : locationResult.getLocations()) {
                    destLocation = new double[]{location.getLatitude(), location.getLongitude()};
                    moveToLocation(destLocation, true);
                }
            };
        };

        requestLocationPermission();
        getLastLocation(true);

        setNavBar();
    }

    public void setNavBar() {
        DrawerLayout mDrawerLayout;
        ActionBarDrawerToggle mActionBarDrawerToggle;
        NavigationView mNavigationView;


        // nav bar
        mDrawerLayout = findViewById(R.id.navigation_drawer);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.navigation_drawer_items);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                Intent intent;
                switch (id) {
                    case R.id.create_contacts:
                        //Toast.makeText(MainActivity.this, "Contacts", Toast.LENGTH_SHORT).show();
                        intent = new Intent(getApplicationContext(), ContactsActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.edit_user_settings:
                        //Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                        intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        break;

                }
                return true;
            }
        });
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
    }

    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void getLastLocation(final boolean zoom) {
        try {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        currLocation = new double[]{location.getLatitude(), location.getLongitude()};
                        moveToLocation(currLocation, zoom);
                    }
                }
            });
        } catch (SecurityException s) {
            //s.printStackTrace();
            requestLocationPermission();
        }
    }

    public void moveToLocation(double[] latlng, boolean moveCamera) {
        LatLng currLocation = new LatLng(latlng[0], latlng[1]);

        try {
            Address address = mGcd.getFromLocation(latlng[0], latlng[1], 1).get(0);
            if (!address.getAddressLine(0).isEmpty()) {
                mMap.addMarker(new MarkerOptions().position(currLocation).title(address.getAddressLine(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(moveCamera)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 12));
    }

    public void keepCurrentLocationUpdated(){
        try {
            // gets current location
            getLastLocation(false);

            moveToLocation(currLocation, true);
            moveToLocation(destLocation, false);

        } catch(Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    getLastLocation(true);
                } else {
                    // permission denied, boo!
                    System.exit(1);
                }
            }
        }
    }


    public void plotRoute() {
        LatLng origin = new LatLng(currLocation[0], currLocation[1]);
        LatLng dest = new LatLng(destLocation[0], destLocation[1]);

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        Toast.makeText(MainActivity.this, "Downloading route data...", Toast.LENGTH_SHORT).show();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String apiKey = "key=AIzaSyBnbmC3yqTdlhDq3iWW8DChz_r-uXrdaf4";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + apiKey;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private class DownloadTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String data = "";

            try {

                data = downloadUrl((String) objects[0]);

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute((String)result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }

    }

    public void respondToHelpButton(View view){
//        TextView help = findViewById(R.id.helpString);
//        String helpString = "";
//        if(help != null)
//            helpString = help.toString();
//
//        if(!helpString.isEmpty() )
//            helpMessage = helpString;

//        for(TestContact i: m_contactList) {
//            String toPhoneNumber = "+1"+ i.phoneNumber;
            String toPhoneNumber = "+1"+ "2018878613";
            String fromPhoneNumber = "+12015747548";
            sendSms(toPhoneNumber, fromPhoneNumber, "I need help. You can track my location below");
//        }

        System.out.println( " Passed checkpoint 1");
    }

    private void sendSms(String toPhoneNumber, String fromPhoneNumber, String message){

        System.out.println( " Passed checkpoint 2");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy( new StrictMode.ThreadPolicy.Builder().permitAll().build() );
        }

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.twilio.com/2010-04-01/Accounts/"+"AC0833244b1a144c6584b596efc516a0d7"+"/SMS/Messages";
        String base64EncodedCredentials = "Basic " + Base64.encodeToString(("AC0833244b1a144c6584b596efc516a0d7" + ":" + "9bfbf8e52950ac58f600387cead87e1f").getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("From", fromPhoneNumber)
                .add("To", toPhoneNumber)
                .add("Body", message)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", base64EncodedCredentials)
                .build();
        try {
            Response response = client.newCall(request).execute();
//            Log.d(TAG, "sendSms: "+ response.body().string());

            System.out.println("sendSms: "+ response.body().string());

        } catch (IOException e) { e.printStackTrace(); }

    }
}