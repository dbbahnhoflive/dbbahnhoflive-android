package de.deutschebahn.bahnhoflive.persistence;

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;

public class LegacyHafasStationItemAdapter extends HafasStationItemAdapter {
    @Override
    public String getId(HafasStation item) {
        return item.id;
    }
}
