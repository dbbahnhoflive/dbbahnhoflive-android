/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import de.deutschebahn.bahnhoflive.R;

public class StatusPreviewButton extends CardButton {

    private ProgressBar progressView;
    private TextView badgeView;
    private boolean showProgress;

    public StatusPreviewButton(@NonNull Context context) {
        super(context);
    }

    public StatusPreviewButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusPreviewButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusPreviewButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onInit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.onInit(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.StatusPreviewButton, defStyleAttr, defStyleRes);

        progressView = findViewById(R.id.progress);
        badgeView = findViewById(R.id.badge);

        showProgress = typedArray.getBoolean(R.styleable.StatusPreviewButton_showProgress, false);

        final CharSequence badgeText = typedArray.getText(R.styleable.StatusPreviewButton_badgeText);
        setBadgeText(badgeText);

        typedArray.recycle();
    }

    @Override
    protected void setViews(Context context) {
        setViews(context, R.layout.decoration_status_preview_button, R.id.content);
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        imageView.setScaleType(scaleType);
    }

    public void setBadgeText(CharSequence text) {
        badgeView.setVisibility(text == null ? INVISIBLE : VISIBLE);
        badgeView.setText(text);

        updateProgressVisibility();
    }

    private void updateProgressVisibility() {
        progressView.setVisibility(showProgress && badgeView.getVisibility() == GONE ? VISIBLE : GONE);
    }

    public void setBadgeDrawable(@DrawableRes int resId) {
//        badgeView.setBackgroundResource(resId);
    }
}
