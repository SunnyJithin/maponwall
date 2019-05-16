package com.jcapp.mapwallpaper;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.jcapp.mapwallpaper.models.Style;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StyleListAdapter extends RecyclerView.Adapter<StyleListAdapter.ItemRowHolder> {

    private List<Style> styleList;
    private Context     mContext;
    RecyclerViewClickListener clickListener;

    StyleListAdapter(Context context, List<Style> styleList, RecyclerViewClickListener recyclerViewClickListener) {
        this.styleList = styleList;
        this.mContext = context;
        this.clickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_style_list, viewGroup, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRowHolder itemRowHolder, int i) {
        itemRowHolder.bindView(i);
    }

    @Override
    public int getItemCount() {
        return (null != styleList ? styleList.size() : 0);
    }

    class ItemRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnMapReadyCallback {

        @BindView(R.id.lite_listrow_map)
        MapView mapView;

        GoogleMap map;
        View      layout;


        ItemRowHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            layout = view;
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
            view.setOnClickListener(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            map = googleMap;
            setMapLocation();
        }

        private void bindView(int pos) {
            Style item = styleList.get(pos);
            // Store a reference of the ViewHolder object in the layout.
            layout.setTag(this);
            // Store a reference to the item in the mapView's tag. We use it to get the
            // coordinate of a location, when setting the map location.
            mapView.setTag(item);
            setMapLocation();
        }


        /**
         * Displays a on a         * {@link com.google.android.gms.maps.GoogleMap}.
         * Adds a marker and centers the camera on the NamedLocation with the normal map type.
         */
        private void setMapLocation() {
            if (map == null) {
                return;
            }
            int position = getAdapterPosition();

            LatLng latLng = new LatLng(43.11, -3.70);
            // Add a marker for this item and set the camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f));
            mapView.setClickable(false);

            if (position == 1) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return;
            }
            try {
                Style style = (Style) mapView.getTag();
                if (style == null) {
                    return;
                }

                int id = mContext.getResources().getIdentifier(style.getName(), "raw", mContext.getPackageName());
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                boolean success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(mContext, id));
                if (!success) {
                    Log.e("WALL", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("WALL", "Can't find style. Error: ", e);
            }
        }


        @Override
        public void onClick(View view) {
            clickListener.onClick(getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onClick(int position);
    }
}


