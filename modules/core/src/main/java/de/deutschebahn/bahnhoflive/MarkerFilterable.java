package de.deutschebahn.bahnhoflive;

public interface MarkerFilterable {
    boolean isFiltered(Object filter, boolean fallback);
}
