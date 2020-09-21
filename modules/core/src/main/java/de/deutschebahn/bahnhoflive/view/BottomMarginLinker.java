/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view;

import android.view.View;
import android.view.ViewGroup;

public class BottomMarginLinker implements View.OnLayoutChangeListener {
    private final int initialBottomMargin;
    private final ViewGroup.MarginLayoutParams layoutParams;

    public BottomMarginLinker(View targetView) {
        layoutParams = (ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
        initialBottomMargin = layoutParams.bottomMargin;
    }

    @Override
    public void onLayoutChange(View observedView, int left, int top, int right, int bottom, int i3, int i4, int i5, int i6) {
        layoutParams.bottomMargin = initialBottomMargin + bottom - top;
    }
}
