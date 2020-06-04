package de.deutschebahn.bahnhoflive.view;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;

public class SingleSelectionManager {

    public interface Listener {
        void onSelectionChanged(SingleSelectionManager selectionManager);
    }

    public static final int INVALID_SELECTION = -1;
    public static String type = null;

    private final List<Listener> listeners = new ArrayList<>();

    @Nullable
    private RecyclerView.Adapter adapter;

    private int selection = INVALID_SELECTION;

    public SingleSelectionManager(@Nullable RecyclerView.Adapter adapter) {
        setAdapter(adapter);
    }

    public void setAdapter(@Nullable RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    public void setSelection(int selection) {
        if (selection == this.selection) {
            return;
        }

        final int previousSelection = this.selection;
        this.selection = selection;

        // Mark tutorial as seen once user opens a cell
        TutorialManager.getInstance(null).markTutorialAsSeen(type);

        notifyListeners();

        notifiyItemChange(previousSelection);
        notifiyItemChange(selection);
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onSelectionChanged(this);
        }
    }

    public void clearSelection() {
        setSelection(INVALID_SELECTION);
    }

    private void notifiyItemChange(int position) {
        if (adapter != null) {
            if (position >= 0 && position < adapter.getItemCount()) {
                adapter.notifyItemChanged(position);
            }
        }
    }

    public boolean isSelected(int position) {
        return position == selection;
    }

    public int getSelection() {
        return selection;
    }

    public <T> T getSelectedItem(List<T> items) {
        return getSelectedItem(items, 0);
    }

    @Nullable
    public <T> T getSelectedItem(List<T> items, int offset) {
        final int index = this.selection - offset;
        if (items != null && index >= 0 && index < items.size()) {
            return items.get(index);
        }

        return null;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
}
