/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.rx

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.util.Cancellable
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe

open class CancellableMaybeOnSubscribe<T>(val action: (VolleyRestListener<T>) -> Cancellable?) :
    MaybeOnSubscribe<T> {
    override fun subscribe(emitter: MaybeEmitter<T>) {
        emitter.setCancellable {
            action(EmitterRestListener(emitter))?.toRxCancellable()
        }
    }
}

fun Cancellable.toRxCancellable() =
    io.reactivex.functions.Cancellable { this@toRxCancellable.cancel() }