package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo;

public class TimetableViewHelper {

    @NonNull
    public static String composeName(TrainInfo trainInfo, TrainMovementInfo trainMovementInfo){
        if (trainMovementInfo != null && !TextUtils.isEmpty(trainMovementInfo.getLineIdentifier())) {
            return (String.format(Locale.GERMAN, "%s %s",
                    trainInfo.getTrainCategory(),
                    trainMovementInfo.getLineIdentifier()));
        } else {
            String trainName = trainInfo.getTrainName();
            if(trainName == null){
                trainName = "";
            }
            return (String.format(Locale.GERMAN, "%s %s",
                    trainInfo.getTrainCategory(),
                    trainName));
        }
    }


    /**
     * @return Might return null if the train doesn't support Wagenstand Requests.
     */
    public static @NonNull
    Map<String, Object> buildQueryParameters(final TrainInfo trainInfo, final TrainMovementInfo event) {

        Map<String,Object> parameters = new HashMap<>();

        try {
            final String trainType = trainInfo.getTrainCategory();

            parameters.put("platform", event.getPlatform().replaceAll("\\D+", ""));

            if (trainType.equals("RE") || trainType.equals("RB")) {
                if (trainInfo.getTrainGenericName() != null) {
                    parameters.put("trainNumber", trainInfo.getTrainGenericName());
                } else {
                    parameters.put("trainNumber", trainInfo.getTrainName());
                }
            } else {
                parameters.put("time", event.getFormattedTime());
                parameters.put("trainNumber", trainInfo.getTrainName());
            }

            Map<String, String> timeOffset = new HashMap<>();
            timeOffset.put("before", "10");
            timeOffset.put("after", "10");
            parameters.put("timeOffset", timeOffset);

            parameters.put("trainType", trainInfo.getTrainCategory());

            parameters.put("trainId", trainInfo.getId());
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        return parameters;
    }
}
