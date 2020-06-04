package de.deutschebahn.bahnhoflive.util.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Hashtable;

public class FontUtil {

	private static Typeface dbPicto;

	private static final String TAG = "Typefaces";

	private static final Hashtable<String, Typeface> cache = new Hashtable<>();

	public static Typeface get(Context c, String assetPath) {
		synchronized (cache) {
			if (!cache.containsKey(assetPath)) {
				try {
					Typeface t = Typeface.createFromAsset(c.getAssets(),
							assetPath);
					cache.put(assetPath, t);
				} catch (Exception e) {
					Log.e(TAG, "Could not get typeface '" + assetPath
							+ "' because " + e.getMessage());
					return null;
				}
			}
			return cache.get(assetPath);
		}
	}

	public static void init(Context context) {
		dbPicto = get(context, "fonts/dbpicto.ttf");
	}

    @Nullable
    public static Typeface getDbPicto() {
        return dbPicto;
    }

}
