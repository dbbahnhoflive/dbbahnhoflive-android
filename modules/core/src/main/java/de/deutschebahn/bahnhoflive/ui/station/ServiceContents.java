package de.deutschebahn.bahnhoflive.ui.station;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;

public class ServiceContents {

    public static class ThreeSComponents {
        public final CharSequence description;
        public final String phoneNumber;

        public ThreeSComponents(ServiceContent serviceContent) {
            final ArrayList<String> strings = parseDreiSComponents(serviceContent.getDescriptionText());

            description = Html.fromHtml(strings.get(0));
            phoneNumber = strings.get(1);
        }
    }

    public static ArrayList<String> parseDreiSComponents(String fromString) {
        ArrayList<String> components = new ArrayList<>();
        Pattern p = Pattern.compile("<p>.*</p>");
        Matcher m = p.matcher(fromString);
        if (m.find()) {
            String descriptionText = m.group();
            String phoneNumber = fromString.substring(m.group().length());
            components.add(descriptionText);
            components.add(phoneNumber);
        } else {
            components.add(fromString);
        }
        return components;
    }

}
