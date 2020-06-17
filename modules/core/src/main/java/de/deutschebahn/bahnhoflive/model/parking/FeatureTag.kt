package de.deutschebahn.bahnhoflive.model.parking

import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R

enum class FeatureTag(@StringRes val label: Int) {
    HAS_CHARGING_STATION(R.string.parking_feature_charging_station),
    IS_LIGHTED(R.string.parking_feature_lighted),
    HAS_PARENT_CHILD_PLACES(R.string.parking_feature_family_places),
    HAS_LIFT(R.string.parking_feature_lift),
    HAS_TOILETS(R.string.parking_feature_toilets),
    HAS_WOMEN_PLACES(R.string.parking_feature_women_places),
    HAS_DISABLED_PLACES(R.string.parking_feature_disabled_places)
}