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

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import de.deutschebahn.bahnhoflive.R;

public class DecoratedCard extends DecorationFrameLayout {

    public DecoratedCard(Context context) {
        this(context, null);
    }

    public DecoratedCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DecoratedCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DecoratedCard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onInit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setViews(context, R.layout.decoration_card, R.id.content);

        final TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.DecoratedCard, defStyleAttr, defStyleRes);

        final float cardCornerRadius = typedArray.getDimension(R.styleable.DecoratedCard_cardCornerRadius, -1);
        if (cardCornerRadius >= 0) {
            final CardView cardView = findViewById(R.id.content);
            cardView.setRadius(cardCornerRadius);
        }

        typedArray.recycle();
    }
}
