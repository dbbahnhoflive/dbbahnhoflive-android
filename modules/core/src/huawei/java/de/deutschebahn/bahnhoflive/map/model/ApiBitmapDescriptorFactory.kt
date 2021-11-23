package de.deutschebahn.bahnhoflive.map.model

import android.graphics.Bitmap
import com.huawei.hms.maps.model.BitmapDescriptorFactory

class ApiBitmapDescriptorFactory {
    companion object {
        @JvmStatic
        fun fromResource(iconResId: Int): ApiBitmapDescriptor =
            ApiBitmapDescriptor(BitmapDescriptorFactory.fromResource(iconResId))

        @JvmStatic
        fun fromBitmap(bitmap: Bitmap): ApiBitmapDescriptor =
            ApiBitmapDescriptor(BitmapDescriptorFactory.fromBitmap(bitmap))
    }
}