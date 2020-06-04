package de.deutschebahn.bahnhoflive.view;

import android.view.View;
import android.view.ViewGroup;

public class LongClickSelectableItemViewHolder<T> extends SelectableItemViewHolder<T> {
    public LongClickSelectableItemViewHolder(ViewGroup parent, int layout, SingleSelectionManager singleSelectionManager) {
        super(parent, layout, singleSelectionManager);
    }

    @Override
    protected void prepareEventListener(View itemView) {
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toggleSelection();

                return true;
            }
        });
    }
}
