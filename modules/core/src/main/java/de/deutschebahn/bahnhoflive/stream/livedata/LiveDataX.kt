package de.deutschebahn.bahnhoflive.stream.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

inline fun <X, Y> LiveData<X>.switchMap(
    crossinline transform: (X) -> LiveData<Y>?
): LiveData<Y> = Transformations.switchMap(this) { transform(it) }
