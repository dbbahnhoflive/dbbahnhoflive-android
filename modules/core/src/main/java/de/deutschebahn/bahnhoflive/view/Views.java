/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public final class Views {
    @NonNull
    public static View inflate(@NonNull ViewGroup parent, @LayoutRes int layout) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }
}
