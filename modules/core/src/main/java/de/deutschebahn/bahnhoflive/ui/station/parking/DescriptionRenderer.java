package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;

abstract class DescriptionRenderer {

    public final static DescriptionRenderer BRIEF = new DescriptionRenderer() {
        @Override
        protected void addDetails(BahnparkSite bahnparkSite, List<String> descriptionParts) {
            addDescriptionDetail(descriptionParts, "Zufahrt", bahnparkSite.getParkraumZufahrt());
            addDescriptionDetail(descriptionParts, "Öffnungszeiten", bahnparkSite.getParkraumOeffnungszeiten());
            addDescriptionDetail(descriptionParts, "Frei parken", bahnparkSite.getTarifFreiparkzeit());
            addDescriptionDetail(descriptionParts, "Maximale Parkdauer", bahnparkSite.getTarifParkdauer());
            final String parkraumEntfernung = bahnparkSite.getParkraumEntfernung();
            addDescriptionDetail(descriptionParts, "Nächster Bahnhofseingang", TextUtils.isEmpty(parkraumEntfernung) ? null : String.format("%s Meter", parkraumEntfernung));
        }
    };

    public final static DescriptionRenderer DETAILED = new DescriptionRenderer() {
        @Override
        protected void addDetails(BahnparkSite bahnparkSite, List<String> descriptionParts) {
            addDescriptionDetail(descriptionParts, "Zufahrt", bahnparkSite.getParkraumZufahrt());
            addDescriptionDetail(descriptionParts, "Öffnungszeiten", bahnparkSite.getParkraumOeffnungszeiten());
            addDescriptionDetail(descriptionParts, "Maximale Parkdauer", bahnparkSite.getTarifParkdauer());
            addDescriptionDetail(descriptionParts, "Parkart", bahnparkSite.getParkraumParkart());
            final String parkraumEntfernung = bahnparkSite.getParkraumEntfernung();
            addDescriptionDetail(descriptionParts, "Nächster Bahnhofseingang", TextUtils.isEmpty(parkraumEntfernung) ? null : String.format("%s Meter", parkraumEntfernung));
            addDescriptionDetail(descriptionParts, "Parktechnik", bahnparkSite.getParkraumTechnik());
            addDescriptionDetail(descriptionParts, "Stellplätze", bahnparkSite.getParkraumStellplaetze());
            addDescriptionDetail(descriptionParts, "Betreiber", bahnparkSite.getParkraumBetreiber());
        }
    };

    public final static DescriptionRenderer PRICE = new DescriptionRenderer() {
        @Override
        protected void addDetails(BahnparkSite bahnparkSite, List<String> descriptionParts) {
            addDescriptionDetail(descriptionParts, "Frei parken", bahnparkSite.getTarifFreiparkzeit());
            addDescriptionDetail(descriptionParts, "1 Stunde", bahnparkSite.getTarif1Std());
            addDescriptionDetail(descriptionParts, "1 Tag", bahnparkSite.getTarif1Tag());
            addDescriptionDetail(descriptionParts, "1 Tag mit BahnCard", bahnparkSite.getTarif1TagRabattDB());
            addDescriptionDetail(descriptionParts, "1 Woche", bahnparkSite.getTarif1Woche());
            addDescriptionDetail(descriptionParts, "1 Woche mit BahnCard", bahnparkSite.getTarif1WocheRabattDB());
            addDescriptionDetail(descriptionParts, "1 Monat Dauerparken", bahnparkSite.getTarif1MonatDauerparken());
            addDescriptionDetail(descriptionParts, "1 Monat Dauerparken (fester Stellplatz)", bahnparkSite.getTarif1MonatDauerparkenFesterStellplatz());
        }

        @Override
        protected String compileRawText(BahnparkSite bahnparkSite) {
            return "Alle Angaben in EUR, inkl. MwSt.<br/>" + super.compileRawText(bahnparkSite);
        }
    };


    protected void addDescriptionDetail(final List<String> details, final String label, String value) {
        if (value == null || TextUtils.isEmpty(value = value.trim())) {
            return;
        }

        details.add("<b>" + label + ": </b>" + TextUtils.htmlEncode(value));
    }

    public CharSequence render(BahnparkSite bahnparkSite) {
        final String rawText = compileRawText(bahnparkSite);
        return Html.fromHtml(rawText);
    }

    protected String compileRawText(BahnparkSite bahnparkSite) {
        List<String> descriptionParts = new ArrayList<>();

        addDetails(bahnparkSite, descriptionParts);

        return TextUtils.join("<br/>", descriptionParts);
    }

    protected abstract void addDetails(BahnparkSite bahnparkSite, List<String> descriptionParts);
}
