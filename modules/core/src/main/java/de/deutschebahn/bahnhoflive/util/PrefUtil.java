package de.deutschebahn.bahnhoflive.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public class PrefUtil {

	private static final String SAVED_FACILITIES = "38";
	private static final String FACILITIES_PUSH_ACTIVE = "39";


	private static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}


	/*
	 *
	 *
	 * CACHING
	 *
	 *
	 */


	public static void storeAlarmKey(@NonNull final String key, @NonNull Context context) {

		final String alarmKey = String.format("alarm_%s", key);

		getPrefs(context)
				.edit()
				.putString(alarmKey, key)
				.commit();

	}

	public static void cleanAlarmKey(@NonNull  String key, @NonNull  Context context) {
		final String alarmKey = String.format("alarm_%s", key);
		getPrefs(context).edit().remove(alarmKey).commit();
	}

	public static boolean hasAlarmSet(@NonNull final String key, @NonNull Context context) {
		final String alarmKey = String.format("alarm_%s", key);
		return getPrefs(context).contains(alarmKey);
	}


	public static List<FacilityStatus> getSavedFacilities(Context context) {
		String savedString = getPrefs(context).getString(SAVED_FACILITIES, "[]");
		return FacilityStatus.fromString(savedString);
	}



	public static boolean getFacilitySubscribed(Context context, int equipmentNumber) {
		List<FacilityStatus> facilities = getSavedFacilities(context);
		for (FacilityStatus f : facilities) {
			if (f.getEquipmentNumber() == equipmentNumber) {
				return f.isSubscribed();
			}
		}
		return false;
	}

	public static void setFacilitySubcribed(Context context, FacilityStatus facility, boolean isChecked) {
		List<FacilityStatus> savedFacilities = getSavedFacilities(context);
		for (FacilityStatus fs : savedFacilities) {
			if (fs.getEquipmentNumber() == facility.getEquipmentNumber()) {
				//modifiy saved facility
				fs.setSubscribed(isChecked);
				storeSavedFacilities(context, savedFacilities);
				return;
			}
		}

		//add facility
		facility.setSubscribed(isChecked);
		savedFacilities.add(facility);
		storeSavedFacilities(context, savedFacilities);
	}

	public static void storeSavedFacilities(Context context, List<FacilityStatus> facilities) {
		//sort by station name and then by description
		Collections.sort(facilities, new Comparator<FacilityStatus>() {
			@Override
			public int compare(FacilityStatus f1, FacilityStatus f2) {
				int res = f1.getStationName().compareTo( f2.getStationName() );
				if(res == 0){
					return f1.getDescription().compareTo( f2.getDescription() );
				}
				return res;
			}
		});
		
		getPrefs(context)
				.edit()
				.putString(SAVED_FACILITIES, FacilityStatus.toString(facilities))
				.commit();
	}

	public static void removeSavedFacilityStatus(Context context, FacilityStatus facilityStatus) {
		List<FacilityStatus> savedFacilities = getSavedFacilities(context);
		for (FacilityStatus fs : savedFacilities) {
			if (fs.getEquipmentNumber() == facilityStatus.getEquipmentNumber()) {
				savedFacilities.remove(fs);
				storeSavedFacilities(context, savedFacilities);
				return;
			}
		}
	}

	public static boolean getFacilityPushActivated(Context context) {
		return getPrefs(context).getBoolean(FACILITIES_PUSH_ACTIVE, true);
	}

	public static void setFacilityGlobalPushActivated(Context context, boolean active) {
		getPrefs(context)
				.edit()
				.putBoolean(FACILITIES_PUSH_ACTIVE,active)
				.commit();
	}

}
