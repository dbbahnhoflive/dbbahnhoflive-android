package de.deutschebahn.bahnhoflive.backend.local.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.deutschebahn.bahnhoflive.util.JSONHelper;

public class TimeRange {
	
	public static final String TYPE = "type";
	public static final String MODIFIEDAT = "modifiedAt";
	public static final String ID = "id";
	public static final String HOURFROM = "hourFrom";
	public static final String HOURUNTIL = "hourUntil";
	public static final String MINUTEFROM = "minuteFrom";
	public static final String MINUTEUNTIL = "minuteUntil";
	

	private String type;
	private String modifiedAt;
	private String id;
	private int hourFrom;
	private int hourUntil;
	private int minuteFrom;
	private int minuteUntil;
	
	public static TimeRange fromJSON(JSONObject timeRange) {
		if (timeRange!=null) {
			TimeRange result = new TimeRange();
			result.setType(JSONHelper.getStringFromJson(timeRange,TYPE));
			result.setId(JSONHelper.getStringFromJson(timeRange,ID));
			result.setModifiedAt(JSONHelper.getStringFromJson(timeRange,MODIFIEDAT));
			try {
				result.setHourFrom(timeRange.getInt(HOURFROM));
				result.setHourUntil(timeRange.getInt(HOURUNTIL));
				result.setMinuteFrom(timeRange.getInt(MINUTEFROM));
				result.setMinuteUntil(timeRange.getInt(MINUTEUNTIL));
				
				return result;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return null;
	}
	
	public static List<TimeRange> fromJSON(JSONArray definition) {
		List<TimeRange> result = new ArrayList<>();
		if (definition != null) {
			for (int i=0;i<definition.length();i++) {
				try {
					TimeRange range = TimeRange.fromJSON(definition.getJSONObject(i));
					if (range!=null) {
						result.add(range);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getHourFrom() {
		return hourFrom;
	}

	public void setHourFrom(int hourFrom) {
		this.hourFrom = hourFrom;
	}

	public int getHourUntil() {
		return hourUntil;
	}

	public void setHourUntil(int hourUntil) {
		this.hourUntil = hourUntil;
	}

	public int getMinuteFrom() {
		return minuteFrom;
	}

	public void setMinuteFrom(int minuteFrom) {
		this.minuteFrom = minuteFrom;
	}

	public int getMinuteUntil() {
		return minuteUntil;
	}

	public void setMinuteUntil(int minuteUntil) {
		this.minuteUntil = minuteUntil;
	}

	public static JSONArray toJSON(List<TimeRange> timeRanges) {
		JSONArray result = new JSONArray();
		if (timeRanges!=null) {
			for (TimeRange tr: timeRanges) {
				JSONObject timeRangeJSON = tr.toJSON();
				if (timeRangeJSON!=null) {
					result.put(timeRangeJSON);
				}
			}
		}
		return result;
	}

	private JSONObject toJSON() {
		JSONObject result = new JSONObject();
		try {
			result.put(ID, getId());
			result.put(TYPE, getType());
			result.put(MODIFIEDAT, getModifiedAt());
			result.put(HOURFROM, getHourFrom());
			result.put(HOURUNTIL, getHourUntil());
			result.put(MINUTEFROM, getMinuteFrom());
			result.put(MINUTEUNTIL, getMinuteUntil());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean matchesTimeRangeWithUntilBeforeMidnight(Calendar cal) {
		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = cal.get(Calendar.MINUTE);
		
		int modifiedHourUntil = hourUntil;
		int modifiedMinuteUntil = minuteUntil;
		if (hourUntil < hourFrom || (hourUntil==hourFrom && minuteUntil <= minuteFrom))  { // 00:00 - 00:00 counts as whole day!
			modifiedHourUntil = 23;
			modifiedMinuteUntil = 59;
		}
		
		
		if (currentHour > modifiedHourUntil || 
				(currentHour==modifiedHourUntil && currentMinute > modifiedMinuteUntil)) {
			//too late
			return false;
		}
		return currentHour >= getHourFrom() &&
				(currentHour != getHourFrom() || currentMinute >= getMinuteFrom());
	}

	public boolean matchesTimeRangeAfterMidnight(Calendar cal) {
		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = cal.get(Calendar.MINUTE);
		
		//only perform check if UNTIL time is "before" FROM time, e.g. 8:00 - 2:00. 
		//the case 02:00 until 02:00 is not supported, only 00:00 until 00:00
		if (getHourFrom() < getHourUntil() || (getHourFrom()==getHourUntil() && getMinuteFrom() < getMinuteUntil())) {
			return false;
		}
		return (currentHour < getHourUntil() || 
				(currentHour== getHourUntil() && currentMinute <= getMinuteUntil()));
	}
}
