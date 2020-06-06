package org.mad.transit.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.model.FavouriteLocation;
import org.mad.transit.repository.FavouriteLocationRepository;

import java.util.ArrayList;
import java.util.List;

public class FavouritePlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final FavouriteLocationRepository favouriteLocationRepository;
    private List<FavouriteLocation> favouriteLocations;
    private final OnItemClickListener onItemClickListener;
    private FavouriteLocation recentlyDeletedFavouriteLocation;
    private int recentlyDeletedItemPosition;

    public FavouritePlacesAdapter(Activity context, FavouriteLocationRepository favouriteLocationRepository, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.favouriteLocationRepository = favouriteLocationRepository;
        this.favouriteLocations = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(this.context).inflate(R.layout.place_favourite_item, parent, false);
        return new RecyclerViewViewHolder(rootView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FavouriteLocation favouriteLocation = this.favouriteLocations.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;
        viewHolder.placeFavouriteItemName.setText(favouriteLocation.getTitle());
        viewHolder.placeFavouriteItemText.setText(favouriteLocation.getLocation().getName());
    }

    @Override
    public int getItemCount() {
        return this.favouriteLocations.size();
    }

    public List<FavouriteLocation> getFavouriteLocations() {
        return this.favouriteLocations;
    }

    public void setFavouriteLocations(List<FavouriteLocation> favouriteLocations) {
        this.favouriteLocations = favouriteLocations;
        this.notifyDataSetChanged();
    }

    public Activity getContext() {
        return this.context;
    }

    public void deleteItem(int position) {
        this.recentlyDeletedFavouriteLocation = this.favouriteLocations.get(position);
        this.recentlyDeletedItemPosition = position;

        this.favouriteLocationRepository.deleteById(String.valueOf(this.recentlyDeletedFavouriteLocation.getId()));
        this.favouriteLocations.remove(position);
        this.notifyItemRemoved(position);
        this.showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        View view = this.context.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(view, this.context.getString(R.string.undo_snack_bar_text, this.recentlyDeletedFavouriteLocation.getTitle()), Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo_snack_bar, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouritePlacesAdapter.this.undoDelete();
            }
        });
        snackbar.show();
    }

    private void undoDelete() {
        this.favouriteLocationRepository.save(this.recentlyDeletedFavouriteLocation);
        this.favouriteLocations.add(this.recentlyDeletedItemPosition, this.recentlyDeletedFavouriteLocation);
        this.notifyItemInserted(this.recentlyDeletedItemPosition);
    }

    private static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView placeFavouriteItemName;
        private final TextView placeFavouriteItemText;

        private RecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            this.placeFavouriteItemName = itemView.findViewById(R.id.place_favourite_item_name);
            this.placeFavouriteItemText = itemView.findViewById(R.id.place_favourite_item_text);

            if (onItemClickListener == null) {
                return;
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(FavouritePlacesAdapter.RecyclerViewViewHolder.this.getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}