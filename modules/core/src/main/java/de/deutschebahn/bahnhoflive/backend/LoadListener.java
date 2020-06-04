package de.deutschebahn.bahnhoflive.backend;

public interface LoadListener<T> {
    void onLoadingDone(T data, int errorCount);
}
