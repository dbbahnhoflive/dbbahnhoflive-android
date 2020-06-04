package de.deutschebahn.bahnhoflive.backend.hafas;

import java.util.List;

public interface Filter<T> {
    int getLimit();

    List<T> filter(List<T> input);
}
