package de.deutschebahn.bahnhoflive.util

import androidx.lifecycle.Observer

/**
 * For keeping [android.arch.lifecycle.LiveData] up to date for later reading.
 */
class DummyObserver<T>: Observer<T> {
    override fun onChanged(t: T?) {
    }
}