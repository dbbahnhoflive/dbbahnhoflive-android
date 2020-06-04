package de.deutschebahn.bahnhoflive.stream.rx

import com.android.volley.Request
import de.deutschebahn.bahnhoflive.backend.RestHelper
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import io.reactivex.functions.Cancellable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.MaybeSubject

class OnSubscribeForRequest<T>(val restHelper: RestHelper, private val factory: (EmitterRestListener<T>) -> Request<*>) : MaybeOnSubscribe<T> {
    override fun subscribe(emitter: MaybeEmitter<T>) {
        val request = factory.invoke(EmitterRestListener(emitter))
        emitter.setCancellable(request.cancellable())
        restHelper.add(request)
    }
}

fun <T> RestHelper.rxQueue(factory: (EmitterRestListener<T>) -> Request<*>) = MaybeSubject.create<T>(
        OnSubscribeForRequest(this, factory)
).subscribeOn(Schedulers.io())

class RequestCancellable(val request: Request<*>) : Cancellable {
    override fun cancel() {
        request.cancel()
    }
}

fun Request<*>.cancellable() = RequestCancellable(this)
