package de.deutschebahn.bahnhoflive.backend;

public interface RestListener<T, E> {
	
    void onSuccess(T payload);

    void onFail(E reason);
}
