/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.models;

import android.content.Context;

import de.deutschebahn.bahnhoflive.R;

public interface WaggonFeatureLabelTemplate {

    WaggonFeatureLabelTemplate DEFAULT = new WaggonFeatureLabelTemplate() {
        @Override
        public CharSequence composeLabel(Context context, WaggonFeature waggonFeature, Status status) {
            if (status.available && waggonFeature.additionalInfo != 0) {
                return status.label == 0 ?
                        context.getString(R.string.template_feature_with_additional_info,
                                context.getString(waggonFeature.label), context.getString(waggonFeature.additionalInfo)) :
                        context.getString(R.string.template_feature_with_status_and_additional_info,
                                context.getString(waggonFeature.label),
                                context.getString(status.label),
                                context.getString(waggonFeature.additionalInfo));
            }

            return status.label == 0 ?
                    context.getString(R.string.template_feature_without_anything,
                            context.getString(waggonFeature.label)) :
                    context.getString(R.string.template_feature_with_status,
                            context.getString(waggonFeature.label),
                            context.getString(status.label));
        }
    };

    CharSequence composeLabel(Context context, WaggonFeature waggonFeature, Status status);
}
