/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import de.deutschebahn.bahnhoflive.R;

public class TextViewImageGetter implements ImageGetter {
    TextView tv;
    private int width;

    public TextViewImageGetter (TextView v, int width) {
        tv = v;
        this.width = width;
    }

    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable levelListDrawable = new LevelListDrawable();

        final Drawable empty = ResourcesCompat.getDrawable(tv.getResources(), R.drawable.placeholder, null);
        if (empty != null) {
            levelListDrawable.addLevel(0, 0, empty);
            levelListDrawable.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        }

        new LoadImage().execute(source, levelListDrawable, tv, width);

        return levelListDrawable;
    }
}