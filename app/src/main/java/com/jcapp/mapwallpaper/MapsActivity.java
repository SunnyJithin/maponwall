package com.jcapp.mapwallpaper;

import android.Manifest;
import android.animation.Animator;
import android.app.WallpaperManager;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.BillingClient;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcapp.mapwallpaper.billing.BillingManager;
import com.jcapp.mapwallpaper.billing.BillingProvider;
import com.jcapp.mapwallpaper.models.Style;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.jcapp.mapwallpaper.AppConstants.SEARCH_LOCATION;
import static com.jcapp.mapwallpaper.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, StyleListAdapter.RecyclerViewClickListener, BillingProvider {

    private static final int    AUTOCOMPLETE_REQUEST_CODE = 888;
    private static final String DIALOG_TAG                = "dialog";


    private GoogleMap googleMap;

    @BindView(R.id.view)
    View view;

    @BindView(R.id.mapView)
    MapView mapView;

    @BindView(R.id.styleList)
    RecyclerView styleRecyclerView;

    @BindView(R.id.wallpaperAnim)
    ConstraintLayout wallpaperAnim;

    @BindView(R.id.group)
    Group group;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.setAsWallpaper)
    CircularProgressButton setAsWallpaper;

    @BindView(R.id.progressBar1)
    ProgressBar progressBar;

    private BillingManager     mBillingManager;
    private MainViewController mViewController;
    private AcquireFragment    mAcquireFragment;
    private static final int    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final        LatLng mDefaultLocation                         =
            new LatLng(-33.8523341, 151.2106085);
    private static final int    DEFAULT_ZOOM                             = 8;
    private static final String SELECTED_POSITION                        = "selected_position";
    private static final String PURCHASE_STATUS                          = "purchase_status";


    private List<Style>                 styles;
    private Bitmap                      bitmap;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean                     mLocationPermissionGranted;
    private Location                    mLastKnownLocation;
    private StyleListAdapter            adapter;
    private boolean                     isOpen     = false;
    private boolean                     showText   = true;
    private boolean                     showMarker = true;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        isNetworkConnectionAvailable();
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
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        if (setAsWallpaper != null) {
            setAsWallpaper.dispose();
        }
        if (bitmap != null) {
            bitmap.recycle();
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
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
                    new DividerItemDecoration(styleRecyclerView.getContext(),
                                              linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(Objects.requireNonNull(
                    ContextCompat.getDrawable(this, R.drawable.itemdecoration)));
            styleRecyclerView.addItemDecoration(dividerItemDecoration);
            styleRecyclerView.setRecyclerListener(mRecycleListener);
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

        if (mBillingManager != null
            && mBillingManager.getBillingClientResponseCode() == BillingClient.BillingResponse.OK) {
            mBillingManager.queryPurchases();
        }
    }

    @OnClick(R.id.search)
    public void search() {
        mFirebaseAnalytics.setCurrentScreen(this, SEARCH_LOCATION, null /* class override */);
        openSearchScreen();
    }

    @OnClick(R.id.saveToGallery)
    public void saveToGallery() {
        int permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        } else {
            saveImage();
        }
    }


    @OnClick(R.id.shareButton)
    public void shareButtonClick() {
        shareBitMap();
    }

    private void shareBitMap() {

        if (bitmap != null) {
            // save bitmap to cache directory
            try {

                File cachePath = new File(getCacheDir(), "images");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(
                        cachePath + "/image.png"); // overwrites this image every time
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            File imagePath = new File(getCacheDir(), "images");
            File newFile   = new File(imagePath, "image.png");
            Uri contentUri =
                    FileProvider.getUriForFile(this, "com.jcapp.mapwallpaper.fileprovider",
                                               newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving
                // app to read this file
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));
            }
        }
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
        setWallPaperSettingsChoser();
    }

    private void setWallPaperSettingsChoser() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View checkBoxView =
                    View.inflate(this, R.layout.dialog_settings_layout, null);
            TextView checkBoxLock = checkBoxView.findViewById(R.id.checkBoxLock);
            TextView checkBoxHome = checkBoxView.findViewById(R.id.checkBoxHome);
            TextView setAsBoth    = checkBoxView.findViewById(R.id.setAsBoth);
            AlertDialog alertDialog = builder.setView(checkBoxView)
                    .show();

            checkBoxLock.setOnClickListener(view -> {
                setWallPaper(WallpaperManager.FLAG_LOCK);
                alertDialog.dismiss();
            });
            checkBoxHome.setOnClickListener(view -> {
                setWallPaper(WallpaperManager.FLAG_SYSTEM);
                alertDialog.dismiss();
            });
            setAsBoth.setOnClickListener(view -> {
                setWallPaper(-1);
                alertDialog.dismiss();
            });


        } else {
            animateAndSetWallpaper(false);
        }
    }

    private void animateAndSetWallpaper(boolean set) {

        setAsWallpaper.startAnimation(); //start loading

        new Handler().postDelayed(() -> {
            WallpaperManager wm = WallpaperManager.getInstance(MapsActivity.this);
            try {
                if (bitmap != null) {
                    if (!set) {
                        wm.setBitmap(bitmap);
                    }
                    setAsWallpaper.revertAnimation(() -> {
                        setAsWallpaper.setText(getString(R.string.success));
                        setAsWallpaper.setBackground(ContextCompat.getDrawable(MapsActivity.this,
                                                                               R.drawable
                                                                                       .button_shape_default_rounded));
                    });
                } else {
                    setAsWallpaper.revertAnimation(() -> {
                        setAsWallpaper.setText(getString(R.string.failed));
                        setAsWallpaper.setBackground(ContextCompat.getDrawable(MapsActivity.this,
                                                                               R.drawable
                                                                                       .button_shape_default_rounded));
                    });
                }
                closePopUp();
            } catch (IOException e) {
                setAsWallpaper.revertAnimation(() -> {
                    setAsWallpaper.setText(getString(R.string.failed));
                    setAsWallpaper.setBackground(ContextCompat.getDrawable(MapsActivity.this,
                                                                           R.drawable
                                                                                   .button_shape_default_rounded));
                });
            }
        }, 1500);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setWallPaper(int condition) {

        new Thread(() -> {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(MapsActivity.this);
                if (wm.isWallpaperSupported()) {
                    switch (condition) {
                        case WallpaperManager.FLAG_LOCK:
                            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                            break;
                        case WallpaperManager.FLAG_SYSTEM:
                            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                            break;
                        default:
                            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        animateAndSetWallpaper(true);
    }

    private void closePopUp() {
        new Handler().postDelayed(() -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();
            params.bottomToBottom = R.id.view;
            mapView.requestLayout();

            wallpaperAnim.setVisibility(View.GONE);
            isOpen = false;
            setAsWallpaper.setText(getString(R.string.set_as_wallpaper));
        }, 500);
    }

    private void checkPermission() {
        captureScreen();
        openWallPaperScreen();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
                    updateLocationUI();

                }

            }

            case 101:
                if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;

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
        progressBar.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();
        params.bottomToBottom = R.id.linearLayout;
        mapView.requestLayout();
        group.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            GoogleMap.SnapshotReadyCallback callback = snapshot -> {
                bitmap = snapshot;
                try {
                    imageView.setImageBitmap(bitmap);
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    group.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            googleMap.snapshot(callback);
        }, 500);
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
            int         size        = inputStream.available();
            byte[]      buffer      = new byte[size];
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
        if (isPremiumPurchased()) {
            setMapStyle(position);
            savePostionInShared(position);
        } else if (position == 0) {
            setMapStyle(position);
            savePostionInShared(position);
        } else {
            showAlertForPro();
        }
    }

    private void setMapStyle(int position) {
        if (position == 1) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return;
        }
        try {
            Style style = styles.get(position);
            int   id    = getResources().getIdentifier(style.getName(), "raw", getPackageName());
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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
                        googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
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
/*

            int x = mainContent.getRight();
            int y = mainContent.getBottom();

            int startRadius = 0;
            int endRadius   = (int) Math.hypot(frameLayout.getWidth(), frameLayout.getHeight());

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(wallpaperAnim, x, y, startRadius,
                                                            endRadius);
*/

             wallpaperAnim.setVisibility(View.VISIBLE);
            //anim.start();

            isOpen = true;

        } else {

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();
            params.bottomToBottom = R.id.view;
            mapView.requestLayout();
            wallpaperAnim.setVisibility(View.GONE);

            isOpen = false;
        }
    }

    private void savePurchaseStatus(boolean purchased) {
        SharedPreferences        sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor     = sharedPref.edit();
        editor.putBoolean(PURCHASE_STATUS, purchased);
        editor.apply();
    }

    private boolean getPurchaseStatus() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(PURCHASE_STATUS, false);
    }


    private void savePostionInShared(int selectedPosition) {
        SharedPreferences        sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor     = sharedPref.edit();
        editor.putInt(SELECTED_POSITION, selectedPosition);
        editor.apply();
    }

    private int getSavedPosition() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(SELECTED_POSITION, 0);
    }

    private void showMarkerInMap(boolean b) {

        if (mLastKnownLocation != null && googleMap != null) {
            double lat       = mLastKnownLocation.getLatitude();
            double longitude = mLastKnownLocation.getLongitude();
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longitude), DEFAULT_ZOOM));
            if (b) {
                googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, longitude)).icon(
                        generateBitmapDescriptorFromRes(MapsActivity.this,
                                                        R.drawable.ic_flag_map_marker)));
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
                Place  place  = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                googleMap.clear();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                if (showMarker) {
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                            generateBitmapDescriptorFromRes(MapsActivity.this,
                                                            R.drawable.ic_flag_map_marker)));
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
        if (mAcquireFragment != null) {
            mAcquireFragment.dismiss();
        }
        if (!getPurchaseStatus()) {
            savePurchaseStatus(true);
            LottieAnimationView animationView = findViewById(R.id.lmi_menu_item);
            animationView.setVisibility(View.VISIBLE);
            animationView.setAnimation(R.raw.success_animation);
            animationView.playAnimation();
            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    animationView.removeAllAnimatorListeners();
                    animationView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
    }

    /**
     * Enables or disables the "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        /*mScreenMain.setVisibility(set ? View.GONE : View.VISIBLE);
        mScreenWait.setVisibility(set ? View.VISIBLE : View.GONE);*/
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    public boolean isPremiumPurchased() {
        return mViewController.isPremiumPurchased();
    }

    private void showAlertForPro() {
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

    private void saveImage() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date             now       = new Date();
        String           fileName  = formatter.format(now) + ".jpg";

        String root  = Environment.getExternalStorageDirectory().toString();
        File   myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + fileName;
        File   file  = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closePopUp();
    }


    /**
     * RecycleListener that completely clears the {@link com.google.android.gms.maps.GoogleMap}
     * attached to a row in the RecyclerView.
     * Sets the map type to {@link com.google.android.gms.maps.GoogleMap#MAP_TYPE_NONE} and clears
     * the map.
     */
    public RecyclerView.RecyclerListener mRecycleListener = holder -> {
        StyleListAdapter.ItemRowHolder mapHolder = (StyleListAdapter.ItemRowHolder) holder;
        if (mapHolder.map != null) {
            // Clear the map and free up resources by changing the map type to none.
            // Also reset the map when it gets reattached to layout, so the previous map would
            // not be displayed.
            mapHolder.map.clear();
            mapHolder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    };

    public void checkNetworkConnection(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        boolean isConnected = activeNetwork != null &&
                              activeNetwork.isConnected();
        if(isConnected) {
            Log.d("Network", "Connected");
            return true;
        }
        else{
            checkNetworkConnection();
            Log.d("Network","Not Connected");
            return false;
        }
    }

}
