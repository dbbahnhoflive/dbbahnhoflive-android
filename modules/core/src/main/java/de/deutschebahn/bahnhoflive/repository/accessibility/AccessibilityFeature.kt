package de.deutschebahn.bahnhoflive.repository.accessibility

import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R

enum class AccessibilityFeature(
    val tag: String,
    @StringRes val label: Int
) {

    STEP_FREE_ACCESS("stepFreeAccess", R.string.accessibilityStepFreeAccess),
    STANDARD_PLATFORM_HEIGHT(
        "standardPlatformHeight",
        R.string.accessibilityStandardPlatformHeight
    ),
    PASSENGER_INFORMATION_DISPLAY(
        "passengerInformationDisplay",
        R.string.accessibilityPassengerInformationDisplay
    ),
    AUDIBLE_SIGNALS_AVAILABLE(
        "audibleSignalsAvailable",
        R.string.accessibilityAudibleSignalsAvailable
    ),
    TACTILE_PLATFORM_ACCESS("tactilePlatformAccess", R.string.accessibilityTactilePlatformAccess),
    TACTILE_GUIDING_STRIPS("tactileGuidingStrips", R.string.accessibilityTactileGuidingStrips),
    TACTILE_HANDRAIL_LABEL("tactileHandrailLabel", R.string.accessibilityTactileHandrailLabel),
    STAIRS_MARKING("stairsMarking", R.string.accessibilityStairsMarking),
    PLATFORM_SIGN("platformSign", R.string.accessibilityPlatformSign),
    AUTOMATIC_DOOR("automaticDoor", R.string.accessibilityAutomaticDoor);

    companion object {
        val VALUES = values()
    }

}