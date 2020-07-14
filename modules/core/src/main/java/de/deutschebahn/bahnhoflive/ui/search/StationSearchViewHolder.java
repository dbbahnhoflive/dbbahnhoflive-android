package de.deutschebahn.bahnhoflive.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker;

public class StationSearchViewHolder extends ViewHolder<SearchResult>
        implements CompoundButton.OnCheckedChangeListener {


    public static final String TAG = StationSearchViewHolder.class.getSimpleName();

    private final TextView nameView;
    private final CompoundButtonChecker favoriteView;
    private final ImageView iconView;

    public StationSearchViewHolder(ViewGroup container, @LayoutRes int layout) {
        this(LayoutInflater.from(container.getContext()).inflate(layout, container, false));
    }

    public StationSearchViewHolder(View view) {
        super(view);

        nameView = itemView.findViewById(R.id.name);

        iconView = itemView.findViewById(R.id.icon);

        favoriteView = new CompoundButtonChecker(itemView.findViewById(R.id.favorite_indicator), this);
    }

    @Override
    protected void onBind(SearchResult item) {
        nameView.setText(item.getTitle());
        iconView.setImageResource(item.getIcon());
        iconView.setContentDescription(iconView.getResources().getText(
                item.isLocal() ? R.string.sr_stop_type_local : R.string.sr_stop_type_db));
        favoriteView.setChecked(item.isFavorite());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final SearchResult searchResult = getItem();
        if (searchResult != null) { // item probably isn't null, but prevent a crash anyways
            searchResult.setFavorite(isChecked);
        }
    }
}
