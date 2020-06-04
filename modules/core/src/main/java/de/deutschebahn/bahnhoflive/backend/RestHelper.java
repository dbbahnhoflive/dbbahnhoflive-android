package de.deutschebahn.bahnhoflive.backend;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;


public class RestHelper {

	@NonNull
	private final RequestQueue queue;

	public RestHelper(@NonNull RequestQueue queue) {
		this.queue = queue;
	}

	public <T> Request<T> add(Request<T> request) {
			return queue.add(request);
	}

}
