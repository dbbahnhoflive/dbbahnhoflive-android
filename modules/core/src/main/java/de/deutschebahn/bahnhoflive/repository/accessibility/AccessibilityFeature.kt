package de.deutschebahn.bahnhoflive.repository.accessibility

import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R

enum class AccessibilityFeature(
    val tag: String,
    @StringRes val label: Int,
    @StringRes val description: Int,
    @StringRes val contentDescription: Int? = null
) {

    STEP_FREE_ACCESS(
        "stepFreeAccess",
        R.string.accessibilityStepFreeAccess,
        R.string.accessibilityDescriptionStepFreeAccess
    ),
    STANDARD_PLATFORM_HEIGHT(
        "standardPlatformHeight",
        R.string.accessibilityStandardPlatformHeight,
        R.string.accessibilityDescriptionStandardPlatformHeight,
        R.string.sr_accessibilityStandardPlatformHeight
    ),
    PASSENGER_INFORMATION_DISPLAY(
        "passengerInformationDisplay",
        R.string.accessibilityPassengerInformationDisplay,
        R.string.accessibilityDescriptionPassengerInformationDisplay
    ),
    AUDIBLE_SIGNALS_AVAILABLE(
        "audibleSignalsAvailable",
        R.string.accessibilityAudibleSignalsAvailable,
        R.string.accessibilityDescriptionAudibleSignalsAvailable
    ),
    TACTILE_PLATFORM_ACCESS(
        "tactilePlatformAccess",
        R.string.accessibilityTactilePlatformAccess,
        R.string.accessibilityDescriptionTactilePlatformAccess
    ),
    TACTILE_GUIDING_STRIPS(
        "tactileGuidingStrips",
        R.string.accessibilityTactileGuidingStrips,
        R.string.accessibilityDescriptionTactileGuidingStrips
    ),
    STAIRS_MARKING(
        "stairsMarking",
        R.string.accessibilityStairsMarking,
        R.string.accessibilityDescriptionStairsMarking
    ),
    TACTILE_HANDRAIL_LABEL(
        "tactileHandrailLabel",
        R.string.accessibilityTactileHandrailLabel,
        R.string.accessibilityDescriptionTactileHandrailLabel
    ),
    PLATFORM_SIGN(
        "platformSign",
        R.string.accessibilityPlatformSign,
        R.string.accessibilityDescriptionPlatformSign
    ),
    AUTOMATIC_DOOR(
        "automaticDoor",
        R.string.accessibilityAutomaticDoor,
        R.string.accessibilityDescriptionAutomaticDoor
    ),
    BOARDING_AID(
        "boardingAid",
        R.string.accessibilityBoardingAid,
        R.string.accessibilityDescriptionBoardingAid);

    companion object {
        val VALUES = values()
    }

}