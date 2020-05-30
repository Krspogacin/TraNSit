package org.mad.transit.util;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
import org.mad.transit.adapters.FavouritePlacesAdapter;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final FavouritePlacesAdapter favouritePlacesAdapter;
    private final Drawable icon;
    private final ColorDrawable background;

    public SwipeToDeleteCallback(FavouritePlacesAdapter favouritePlacesAdapter) {
        super(0, ItemTouchHelper.LEFT);
        this.favouritePlacesAdapter = favouritePlacesAdapter;
        this.icon = ContextCompat.getDrawable(favouritePlacesAdapter.getContext(), R.drawable.ic_delete_white_36dp);
        this.background = new ColorDrawable(favouritePlacesAdapter.getContext().getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX,
                dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        int iconMargin = (itemView.getHeight() - this.icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - this.icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + this.icon.getIntrinsicHeight();

        if (dX < 0) {
            int iconLeft = itemView.getRight() - iconMargin - this.icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            this.icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            this.background.setBounds(itemView.getRight() + ((int) dX),
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            this.background.setBounds(0, 0, 0, 0);
        }

        this.background.draw(c);
        this.icon.draw(c);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        this.favouritePlacesAdapter.deleteItem(position);
    }
}