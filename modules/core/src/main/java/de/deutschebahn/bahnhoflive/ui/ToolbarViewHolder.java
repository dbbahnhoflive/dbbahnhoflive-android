package de.deutschebahn.bahnhoflive.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.R;

public class ToolbarViewHolder extends RecyclerView.ViewHolder {

    private final TextView titleView;

    public ToolbarViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.screen_title);
    }

    public ToolbarViewHolder(View itemView, CharSequence title) {
        this(itemView);
        setTitle(title);
    }

    public ToolbarViewHolder(View itemView, @StringRes int title) {
        this(itemView);
        setTitle(title);
    }


    public void setTitle(int title) {
        setTitle(title == 0 ? null : itemView.getContext().getText(title));
    }

    public void setTitle(CharSequence title) {
        if (titleView != null) {
            titleView.setText(title);
        }
    }
}
