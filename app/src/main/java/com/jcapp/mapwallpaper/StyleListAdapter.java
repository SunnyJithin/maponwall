package com.jcapp.mapwallpaper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcapp.mapwallpaper.models.Style;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StyleListAdapter extends RecyclerView.Adapter<StyleListAdapter.ItemRowHolder> {

    private List<Style> styleList;
    private Context mContext;
    RecyclerViewClickListener clickListener;

    StyleListAdapter(Context context, List<Style> styleList,RecyclerViewClickListener recyclerViewClickListener) {
        this.styleList = styleList;
        this.mContext = context;
        this.clickListener=recyclerViewClickListener;
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_style_list, viewGroup,false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRowHolder itemRowHolder, int i) {
        final String itemName = styleList.get(i).getPosition();
        int id = mContext.getResources().getIdentifier(itemName, "drawable", mContext.getPackageName());
        itemRowHolder.imageView.setBackgroundResource(id);
    }

    @Override
    public int getItemCount() {
        return (null != styleList ? styleList.size() : 0);
    }

    class ItemRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.styleImage)
        ImageView imageView;

        ItemRowHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
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


