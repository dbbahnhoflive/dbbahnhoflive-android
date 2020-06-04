package de.deutschebahn.bahnhoflive.stream.rx

import io.reactivex.Observable

fun <T> Observable<T>.cacheLatest() = replay(1).autoConnect()