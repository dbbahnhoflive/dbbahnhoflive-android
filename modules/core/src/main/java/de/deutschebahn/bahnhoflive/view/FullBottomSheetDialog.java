/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class FullBottomSheetDialog extends BottomSheetDialog {

    public FullBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public FullBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected FullBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        patchBehavior();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        patchBehavior();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        patchBehavior();
    }

    private void patchBehavior() {
        final View bottomSheet = getDelegate().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            final BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setPeekHeight(Integer.MAX_VALUE);
        }
    }
}
