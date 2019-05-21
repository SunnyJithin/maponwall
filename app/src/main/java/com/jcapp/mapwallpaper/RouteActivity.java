package com.jcapp.mapwallpaper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jcapp.mapwallpaper.MapsUtils.generateBitmapDescriptorFromRes;

/**
 * @author Jithin
 */
public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int    AUTOCOMPLETE_START_CODE = 888;
    private static final int    AUTOCOMPLETE_DEST_CODE  = 999;
    public static final  String RES_INT                 = "res_int";
    public static final  String LAT                     = "lat";
    public static final  String LONG                    = "long";

    private int       resourceId;
    private Double    latitude;
    private Double    longitude;
    private Marker    startMarker;
    private Marker    destMarker;
    private GoogleMap googleMap;
    private LatLng    latLng;

    @BindView(R.id.mapView)
    MapView mapView;

    @BindView(R.id.start)
    EditText start;

    @BindView(R.id.desination)
    EditText desination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        ButterKnife.bind(this);
        getExtras();
        setCurretLocation();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void setCurretLocation() {
        if (latitude != null && longitude != null) {
            latLng = new LatLng(latitude, longitude);
            start.setText(getString(R.string.current_location));
            setStartMarker(latLng);
        } else {
            start.setText(getString(R.string.select_location));
        }
    }

    private void getExtras() {
        if (getIntent().getExtras() != null) {
            resourceId = getIntent().getExtras().getInt(RES_INT);
            latitude = getIntent().getExtras().getDouble(LAT);
            longitude = getIntent().getExtras().getDouble(LONG);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    private void openSearchScreen(int startLocation) {
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }


        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, startLocation);

    }

    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example it's place name and place ID).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_START_CODE) {
            if (resultCode == RESULT_OK) {
                Place  place  = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                setStartMarker(latLng);
            }
        } else if (requestCode == AUTOCOMPLETE_DEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place  place  = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if (destMarker != null) {
                    destMarker.remove();
                }

                if (latLng != null) {
                    destMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                            generateBitmapDescriptorFromRes(RouteActivity.this,
                                                            R.drawable.ic_flag_map_marker)));
                }
            }
        }
        setZoomLevel();
        findRoute();

    }

    private void setStartMarker(LatLng latLng) {
        if (startMarker != null) {
            startMarker.remove();
        }
        if (latLng != null) {
            startMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                    generateBitmapDescriptorFromRes(RouteActivity.this,
                                                    R.drawable.ic_location_icon)));
        }
    }


    private void setZoomLevel() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (startMarker != null) {
            builder.include(startMarker.getPosition());
        }

        if (destMarker != null) {
            builder.include(destMarker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu     = CameraUpdateFactory.newLatLngBounds(bounds, 16);
        googleMap.animateCamera(cu);
    }


    private void findRoute() {

        // Getting URL to the Google Directions API
       // String url = getUrl(origin, dest);

        FetchUrl FetchUrl = new FetchUrl();

//
      //  FetchUrl.execute(url);

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... url) {

            String data ="";
            try {
                data = downloadUrl(url[0]);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String            data          = "";
            InputStream       iStream       = null;
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br   = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer   sb   = new StringBuffer();
                String         line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {

            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }


}
