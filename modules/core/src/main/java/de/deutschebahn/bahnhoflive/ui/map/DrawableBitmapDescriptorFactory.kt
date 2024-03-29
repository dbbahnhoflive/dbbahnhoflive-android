/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class DrawableBitmapDescriptorFactory(
    val drawableProvider: (highlighted: Boolean) -> Drawable
) : MarkerContent.BitmapDescriptorFactory {

    override fun createBitmapDescriptor(highlighted: Boolean): BitmapDescriptor {
        val drawable = drawableProvider(highlighted)
        val width = (drawable.getIntrinsicWidth() * highlighted.scale).toInt()
        val height = (drawable.getIntrinsicHeight() * highlighted.scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, width, height)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    val Boolean.scale get() = if (this) 1.2f else 1f
}