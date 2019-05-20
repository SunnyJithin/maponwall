package com.jcapp.mapwallpaper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Jithin
 */
public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String RES_INT = "RES_INT";
    private             int    resourceId;

    private GoogleMap googleMap;

    @BindView(R.id.mapView)
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        ButterKnife.bind(this);
        getExtras();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void getExtras() {
        if (getIntent().getExtras() != null) {
            resourceId = getIntent().getExtras().getInt(RES_INT);
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
}
