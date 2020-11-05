package de.deutschebahn.bahnhoflive.analytics

import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.BuildConfig
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

class SentryIssueTracker(app: BaseApplication, dsn: String) : IssueTracker(app) {

    init {
        SentryAndroid.init(app) { sentryAndroidOptions ->
            sentryAndroidOptions.dsn = dsn
            sentryAndroidOptions.isDebug = BuildConfig.DEBUG
        }

        TaloTracing.addTraceIdListener {
            setTag("taloId", it)
        }
    }

    override fun log(msg: String) {
        Sentry.captureMessage(msg)
    }

    override fun dispatchThrowable(throwable: Throwable, hint: String?) {
        if (hint != null) {
            log(hint)
        }
        Sentry.captureException(throwable, hint) // hint will be ignored by Sentry
    }

    override fun setTag(key: String, value: String?) {
        if (value == null) {
            Sentry.removeTag(key)
        } else {
            Sentry.setTag(key, value)
        }
    }

    override fun setContext(name: String, values: Map<String, out Any>?) {
        Sentry.configureScope { scope ->
            if (values == null) {
                scope.removeContexts(name)
            } else {
                scope.setContexts(name, values.toMap())
            }
        }
    }
}