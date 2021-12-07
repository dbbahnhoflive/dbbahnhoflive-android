package de.deutschebahn.bahnhoflive.ui.feedback

import android.os.Build
import android.text.TextUtils

/** Returns the consumer friendly device name  */
val deviceName: String?
    get() {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

fun capitalize(str: String): String? {
    if (TextUtils.isEmpty(str)) {
        return str
    }
    val arr = str.toCharArray()
    var capitalizeNext = true

    //        String phrase = "";
    val phrase = StringBuilder()
    for (c in arr) {
        if (capitalizeNext && Character.isLetter(c)) {
            //                phrase += Character.toUpperCase(c);
            phrase.append(Character.toUpperCase(c))
            capitalizeNext = false
            continue
        } else if (Character.isWhitespace(c)) {
            capitalizeNext = true
        }
        //            phrase += c;
        phrase.append(c)
    }

    return phrase.toString()
}
