package de.deutschebahn.bahnhoflive.backend.hafas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;

public class LocalTransportFilter extends LimitingFilter<HafasStation> {

    private final List<String> evaIds;

    private int bitmaskLocalTransport;

    public LocalTransportFilter(int limit, int bitmaskLocalTransport) {
        this(limit, null, bitmaskLocalTransport);
    }

    public LocalTransportFilter(int limit, List<String> evaIds, int bitmaskLocalTransport) {
        super(limit);
        this.evaIds = evaIds == null ? Collections.emptyList() : evaIds;
        this.bitmaskLocalTransport = bitmaskLocalTransport;
    }

    @Override
    public List<HafasStation> filter(List<HafasStation> input) {
        final ArrayList<HafasStation> filteredStations = new ArrayList<>(input.size());

        for (HafasStation hafasStation : input) {
            if (accepts(hafasStation) && !evaIds.contains(hafasStation.extId)) {
                filteredStations.add(hafasStation);
            }
        }

        Collections.sort(filteredStations, (o1, o2) -> o1.dist - o2.dist);

        return super.filter(filteredStations);
    }

    protected boolean accepts(HafasStation hafasStation) {
        return hafasStation.hasLocalTransport(bitmaskLocalTransport);
    }
}
