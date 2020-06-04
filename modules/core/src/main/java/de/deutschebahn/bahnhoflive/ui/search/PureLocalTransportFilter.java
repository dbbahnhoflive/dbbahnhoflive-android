package de.deutschebahn.bahnhoflive.ui.search;

import de.deutschebahn.bahnhoflive.backend.hafas.LocalTransportFilter;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory;

class PureLocalTransportFilter extends LocalTransportFilter {
    public PureLocalTransportFilter(int remainingSlots) {
        super(remainingSlots, ProductCategory.BITMASK_LOCAL_TRANSPORT);
    }

    @Override
    protected boolean accepts(HafasStation hafasStation) {
        return hafasStation.isPureLocalTransport();
    }
}
