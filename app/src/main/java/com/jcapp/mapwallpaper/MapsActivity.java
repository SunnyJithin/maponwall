package com.jcapp.mapwallpaper;

import android.Manifest;
import android.animation.Animator;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcapp.mapwallpaper.billing.BillingManager;
import com.jcapp.mapwallpaper.billing.BillingProvider;
import com.jcapp.mapwallpaper.models.Style;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.jcapp.mapwallpaper.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, StyleListAdapter.RecyclerViewClickListener, BillingProvider {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 888;
    private static final String DIALOG_TAG = "dialog";


    private GoogleMap googleMap;

    @BindView(R.id.mapView)
    MapView mapView;

    @BindView(R.id.styleList)
    RecyclerView styleRecyclerView;

    @BindView(R.id.linearLayout)
    ConstraintLayout mainContent;

    @BindView(R.id.wallpaperAnim)
    ConstraintLayout wallpaperAnim;

    @BindView(R.id.frameLayout)
    FrameLayout frameLayout;

    @BindView(R.id.closeButton)
    ImageButton closeButton;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.setAsWallpaper)
    CircularProgressButton setAsWallpaper;

    private BillingManager mBillingManager;
    private MainViewController mViewController;
    private AcquireFragment mAcquireFragment;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 10;
    private static final String SELECTED_POSITION = "selected_position";


    private List<Style> styles;
    private Bitmap bitmap;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private StyleListAdapter adapter;
    private boolean isOpen = false;
    private boolean showText = true;
    private boolean showMarker = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        setStyleRecyclerView(showText);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Create and initialize BillingManager which talks to BillingLibrary

        // Start the controller and load game data
        mViewController = new MainViewController(this);
        mBillingManager = new BillingManager(this, mViewController.getUpdateListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setAsWallpaper.dispose();
        bitmap.recycle();
        mapView.onDestroy();
    }

    private void setStyleRecyclerView(boolean b) {
        if (styles != null) {
            styles.clear();
        } else {
            styles = new ArrayList<>();
        }
        styles.addAll(getStyleList(b));
        if (adapter == null) {
            adapter = new StyleListAdapter(this, styles, this);
            styleRecyclerView.setAdapter(adapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
            styleRecyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(styleRecyclerView.getContext(), linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.itemdecoration)));
            styleRecyclerView.addItemDecoration(dividerItemDecoration);
        } else {
            adapter.notifyDataSetChanged();
        }

        int position = getSavedPosition();
        if (!(position == 0)) {
            if (styleRecyclerView.getLayoutManager() != null) {
                styleRecyclerView.getLayoutManager().scrollToPosition(position);
            }
        }
        if (googleMap != null) {
            setMapStyle(position);
        }
    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @OnClick(R.id.search)
    public void search() {
        openSearchScreen();
    }

    @OnClick(R.id.currentLocation)
    public void currentLocation() {
        getDeviceLocation();
    }

    @OnClick(R.id.showText)
    public void toogleText() {
        showText = !showText;
        setStyleRecyclerView(showText);
    }

    @OnClick(R.id.showMarker)
    public void toogleMarker() {
        showMarker = !showMarker;
        showMarkerInMap(showMarker);
    }


    @OnClick(R.id.captureImage)
    public void submit() {
        checkPermission();
    }

    @OnClick(R.id.closeButton)
    public void closeScreen() {
        openWallPaperScreen();
    }

    @OnClick(R.id.setAsWallpaper)
    public void setAsWallpaper() {

        setAsWallpaper.startAnimation(); //start loading

        new Handler().postDelayed(() -> {
            WallpaperManager wm = WallpaperManager.getInstance(MapsActivity.this);
            try {
                if (bitmap != null) {
                    wm.setBitmap(bitmap);
                    setAsWallpaper.revertAnimation(() -> {
                        setAsWallpaper.setText(getString(R.string.success));
                        setAsWallpaper.setBackground(ContextCompat.getDrawable(MapsActivity.this, R.drawable.button_shape_default_rounded));
                    });
                } else {
                    setAsWallpaper.revertAnimation(() -> {
                        setAsWallpaper.setText(getString(R.string.failed));
                        setAsWallpaper.setBackground(ContextCompat.getDrawable(MapsActivity.this, R.drawable.button_shape_default_rounded));
                    });
                }
                closePopUp();
            } catch (IOException e) {
                setAsWallpaper.revertAnimation(() -> {
                    setAsWallpaper.setText(getString(R.string.failed));
                    setAsWallpaper.setBackground(ContextCompat.getDrawable(MapsActivity.this, R.drawable.button_shape_default_rounded));
                });
            }
        }, 1500);

    }

    private void closePopUp() {
        new Handler().postDelayed(() -> {
            wallpaperAnim.setVisibility(View.GONE);
            isOpen = false;
        }, 1500);
    }

    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            captureScreen();
            openWallPaperScreen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {

            case 100:
                if ((grantResults.length > 0) &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    captureScreen();
                    openWallPaperScreen();
                }
                break;
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                updateLocationUI();

            }
            default:
                break;
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (!mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
        showMarkerInMap(showMarker);
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
        this.googleMap = googleMap;
        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        setMapStyle(getSavedPosition());
    }


    public void captureScreen() {
        GoogleMap.SnapshotReadyCallback callback = snapshot -> {
            // TODO Auto-generated method stub
            bitmap = snapshot;
            OutputStream fout = null;
            String filePath = System.currentTimeMillis() + ".jpeg";
            try {
                fout = openFileOutput(filePath, Context.MODE_PRIVATE);
                // Write the string to the file
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                imageView.setImageBitmap(bitmap);
                fout.flush();
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d("ImageCapture", "FileNotFoundException");
                Log.d("ImageCapture", e.getMessage());
                filePath = "";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("ImageCapture", "IOException");
                Log.d("ImageCapture", e.getMessage());
                filePath = "";
            }

            // openShareImageDialog(filePath);
        };

        googleMap.snapshot(callback);
    }

    public void openShareImageDialog(String filePath) {
        File file = this.getFileStreamPath(filePath);
        if (!filePath.equals("")) {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile =
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values);

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
    }

    public List<Style> getStyleList(boolean showText) {
        String json;
        try {
            int id;
            if (showText) {
                id = R.raw.styles_list;
            } else {
                id = R.raw.styles_list_no_text;
            }
            InputStream inputStream = getResources().openRawResource(id);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<Style>>() {
            }.getType());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(int position) {
        if(isPremiumPurchased()) {
            setMapStyle(position);
            savePostionInShared(position);
        }else if(position==0){
            setMapStyle(position);
            savePostionInShared(position);
        }else {
            showAlertForPro();
        }
    }

    private void setMapStyle(int position) {
        try {
            Style style = styles.get(position);
            int id = getResources().getIdentifier(style.getName(), "raw", getPackageName());
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, id));
            if (!success) {
                Log.e("WALL", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("WALL", "Can't find style. Error: ", e);
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
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        if (mLastKnownLocation != null) {
                            MapsActivity.this.showMarkerInMap(showMarker);
                        }
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        MapsActivity.this.showMarkerInMap(showMarker);
                    }
                });
            } else {
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
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
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onBackPressed() {
        if (isOpen) {
            openWallPaperScreen();
        } else {
            super.onBackPressed();
        }
    }

    private void openWallPaperScreen() {

        if (!isOpen) {

            int x = mainContent.getRight();
            int y = mainContent.getBottom();

            int startRadius = 0;
            int endRadius = (int) Math.hypot(frameLayout.getWidth(), frameLayout.getHeight());

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(wallpaperAnim, x, y, startRadius,
                            endRadius);

            wallpaperAnim.setVisibility(View.VISIBLE);
            anim.start();

            isOpen = true;

        } else {

            int x = wallpaperAnim.getRight();
            int y = wallpaperAnim.getBottom();

            int startRadius = Math.max(mainContent.getWidth(), mainContent.getHeight());
            int endRadius = 0;

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(wallpaperAnim, x, y, startRadius,
                            endRadius);

            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    wallpaperAnim.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.start();

            isOpen = false;
        }
    }

    private void savePostionInShared(int selectedPosition) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SELECTED_POSITION, selectedPosition);
        editor.apply();
    }

    private int getSavedPosition() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(SELECTED_POSITION, 0);
    }

    private void showMarkerInMap(boolean b) {

        if (mLastKnownLocation != null && googleMap != null) {
            double lat = mLastKnownLocation.getLatitude();
            double longitude = mLastKnownLocation.getLongitude();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longitude), DEFAULT_ZOOM));
            if (b) {
                googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, longitude)).icon(
                        generateBitmapDescriptorFromRes(MapsActivity.this, R.drawable.ic_flag_map_marker)));
            } else {
                googleMap.clear();
            }
        }
    }


    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void openSearchScreen() {
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
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }

    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example it's place name and place ID).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                if (showMarker) {
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                            generateBitmapDescriptorFromRes(MapsActivity.this, R.drawable.ic_flag_map_marker)));
                } else {
                    googleMap.clear();
                }
            }
        }
    }


    /**
     * User clicked the "Buy Gas" button - show a purchase dialog with all available SKUs
     */
    public void onPurchaseButtonClicked() {

        if (mAcquireFragment == null) {
            mAcquireFragment = new AcquireFragment();
        }

        if (!isAcquireFragmentShown()) {
            mAcquireFragment.show(getSupportFragmentManager(), DIALOG_TAG);

            if (mBillingManager != null
                    && mBillingManager.getBillingClientResponseCode()
                    > BILLING_MANAGER_NOT_INITIALIZED) {
                mAcquireFragment.onManagerReady(this);
            }
        }
    }

    void onBillingManagerSetupFinished() {
        if (mAcquireFragment != null) {
            mAcquireFragment.onManagerReady(this);
        }
    }

    public boolean isAcquireFragmentShown() {
        return mAcquireFragment != null && mAcquireFragment.isVisible();
    }

    //TODO HANDLE PRO PURCHASE
    public void showRefreshedUi() {

    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    public boolean isPremiumPurchased() {
        return mViewController.isPremiumPurchased();
    }

    private void showAlertForPro(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Upgrade the app for full features")
                .setCancelable(false)
                .setPositiveButton("Upgrade", (dialog, id) -> {
                  onPurchaseButtonClicked();
                    dialog.cancel();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
