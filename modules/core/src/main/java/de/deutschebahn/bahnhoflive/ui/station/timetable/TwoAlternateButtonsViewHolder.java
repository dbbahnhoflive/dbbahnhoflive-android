package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.R;

public class TwoAlternateButtonsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView leftButton;
    private final TextView rightButton;

    private final View.OnClickListener onClickListener;
    private final int leftButtonId;
    private final int rightButtonId;

    public TwoAlternateButtonsViewHolder(View itemView, int leftButtonId, int rightButtonId, View.OnClickListener onClickListener) {
        super(itemView);

        this.onClickListener = onClickListener;
        this.leftButtonId = leftButtonId;
        this.rightButtonId = rightButtonId;

        leftButton = itemView.findViewById(leftButtonId);
        leftButton.setOnClickListener(this);
        rightButton = itemView.findViewById(rightButtonId);
        rightButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == leftButtonId) {
            setChecked(leftButton);
            setUnchecked(rightButton);
        } else if (id == rightButtonId) {
            setChecked(rightButton);
            setUnchecked(leftButton);
        }

        onClickListener.onClick(v);
    }

    private void setUnchecked(TextView textView) {
        applyAttributes(textView, R.color.white, Typeface.NORMAL, 0, 0);
    }

    private void setChecked(TextView textView) {
        applyAttributes(textView, R.color.anthracite, Typeface.BOLD, R.drawable.shape_toggle_thumb,
                textView.getContext().getResources().getDimension(R.dimen.default_elevation));
    }

    private void applyAttributes(TextView textView, int color, int style, int background, float elevation) {
        textView.setTypeface(textView.getTypeface(), style);
        textView.setBackgroundResource(background);
        ViewCompat.setElevation(textView, elevation);
        textView.setTextColor(ContextCompat.getColor(textView.getContext(), color));
    }

    public void checkLeftButton() {
        setChecked(leftButton);
        setUnchecked(rightButton);
    }

    public void checkRightButton() {
        setChecked(rightButton);
        setUnchecked(leftButton);
    }
}
