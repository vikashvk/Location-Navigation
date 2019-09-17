package com.example.application.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnPolylineClickListener,
        LocationListener {

    private GoogleMap mMap;
    private JSONArray steps;
    final int USER_DEDE = 0;
    double begLat, begLong, destLat, destLong,wayLat,wayLong;
    String apiUrl;

//https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyA5rKCO7y5SbNXoy-sRxjgKiJduPj4cVEk

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationKiPermission();
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
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

//google api

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task task = LocationServices.getFusedLocationProviderClient(this).getLastLocation();
        Task task1 = task.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull final Task task) {

                if (task.isSuccessful()) {
                    Location lastloc = (Location) task.getResult();
                    if (lastloc != null) {
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        LatLng ll = new LatLng(lastloc.getLatitude(), lastloc.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(ll);
                        markerOptions.title("I'm here Bro");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 5.0f));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        begLat = lastloc.getLatitude();
                        begLong = lastloc.getLongitude();

                        //touch on map
                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title("Want to go here this weekend");
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                                mMap.addMarker(markerOptions);

                                //move camera
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                                mMap.animateCamera(CameraUpdateFactory.zoomTo(1));
                                destLat = latLng.latitude;
                                destLong = latLng.longitude;

                                apiUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="
                                        + begLat + "," + begLong + "&destination=" + destLat + "," + destLong + "&waypoints=via:26.9124,75.7873|26.4499,74.6399&key=AIzaSyA5rKCO7y5SbNXoy-sRxjgKiJduPj4cVEk";

                                Route route = new Route();
                                route.execute();
//
//                                try {
//                                    getLatLongByName("Jaipur");
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }

                                // Navigation turn by turn

                                Uri getGeoUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + begLat + ","
                                        + begLong + "&destination=" + destLat + "," + destLong +
                                        "&waypoints=26.9124,75.7873|26.4499,74.6399");
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, getGeoUri);
                                startActivity(mapIntent);
                            }
                        });
                    }
                }
            }
        });
    }
//
//    private void getLatLongByName(String location) throws IOException {
//        if (Geocoder.isPresent()) {
//            Geocoder gc = new Geocoder(this);
//
//            List<Address> addresses = gc.getFromLocationName("Jaipur",1);
//            Address way_points=addresses.get(0);
//
//            wayLat=way_points.getLatitude();
//            wayLong=way_points.getLongitude();
//
//
//        }
//    }

    public boolean locationKiPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    ) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, USER_DEDE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, USER_DEDE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case USER_DEDE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationKiPermission();
                } else {
                    closeIt();
                }
            }
        }
    }

    private void closeIt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAffinity();
        } else {
            finish();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class Route extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String current = "";
            try {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(apiUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    int data = inputStreamReader.read();
                    while (data != -1) {
                        current += (char) data;
                        data = inputStreamReader.read();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
            return current;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            try {
                steps = new JSONObject(aVoid).getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                        .getJSONObject(0).getJSONArray("steps");

                for (int i = 0; i < steps.length(); i++) {
                    mMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(steps.getJSONObject(i).getJSONObject("polyline").getString("points"))));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static final PatternItem DOT = new Dot();
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }
        Toast.makeText(this, "Route type " + polyline.getTag().toString(), Toast.LENGTH_SHORT).show();
    }
}