package de.deutschebahn.bahnhoflive.ui.map.content;

public class MapHelper {


	public static int mapBackspinLevelsToGmaps(int backspinLevelIndex) {
		switch (backspinLevelIndex) {
		case 1:
			return 1; //firstFloorLevels
		case 2:
		case 3:
		case -1:
		case 4: return 0;
		default: return 9999;
		}
	}


}
