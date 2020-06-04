package de.deutschebahn.bahnhoflive.ui.map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.deutschebahn.bahnhoflive.R;

public class CommonFlyoutViewHolder extends StatusFlyoutViewHolder implements View.OnClickListener {

    private final View linkButton;

    public final TextView descriptionView;

    public CommonFlyoutViewHolder(ViewGroup parent) {
        super(parent, R.layout.flyout_generic);

        descriptionView = findTextView(R.id.description);

        linkButton = itemView.findViewById(R.id.external_link);
        linkButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        getItem().getMarkerContent().openLink(context);
    }

    @Override
    protected void onBind(MarkerBinder item) {
        super.onBind(item);

        final MarkerContent markerContent = item.getMarkerContent();
        descriptionView.setText(markerContent.getDescription(context));

        linkButton.setVisibility(markerContent.hasLink() ? View.VISIBLE : View.GONE);
    }

}
