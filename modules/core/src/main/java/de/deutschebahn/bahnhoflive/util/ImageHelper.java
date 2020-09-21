/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

import de.deutschebahn.bahnhoflive.R;

public class ImageHelper {
    public static int getImagewidthTarget(Activity act) {
        Display display = act.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
//		int height = size.y;
        int servicePadding = act.getResources().getDimensionPixelOffset(R.dimen.service_paddingH);
        return width - 2*servicePadding;
    }
}
