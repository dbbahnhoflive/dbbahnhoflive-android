/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;

import de.deutschebahn.bahnhoflive.BaseApplication;

public class VectorBitmapDescriptorFactory {
    @NonNull
    public static BitmapDescriptor createBitmapDescriptor(@DrawableRes int drawableId) {
        return createBitmapDescriptor(drawableId, 1.0f);
    }

    @NonNull
    public static BitmapDescriptor createBitmapDescriptor(@DrawableRes int drawableId, float scale) {
        final BaseApplication context = BaseApplication.get();

        final Drawable drawable = context.getResources().getDrawable(drawableId);

        if (scale == 1.0f && drawable instanceof BitmapDrawable) {
            return BitmapDescriptorFactory.fromResource(drawableId);
        }

        final int width = (int) (drawable.getIntrinsicWidth() * scale);
        final int height = (int) (drawable.getIntrinsicHeight() * scale);
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, width, height);
        final Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
