/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.view.CardButton;

public interface Category {
    @NonNull
    CategorySelectionListener getSelectionListener();

    void bind(CardButton cardButton);

    String getTrackingTag();

    interface CategorySelectionListener {
        void onCategorySelected(@NonNull Category category);
    }
}
