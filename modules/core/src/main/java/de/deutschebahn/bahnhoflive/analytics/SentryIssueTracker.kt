package de.deutschebahn.bahnhoflive.analytics

import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.util.isAppDead
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid

class SentryIssueTracker(app: BaseApplication, dsn: String) : IssueTracker(app) {

    init {
       if(!isAppDead()) {
        SentryAndroid.init(app) { sentryAndroidOptions ->
            sentryAndroidOptions.dsn = dsn
            sentryAndroidOptions.isDebug = BuildConfig.DEBUG

            if(BuildConfig.DEBUG)
              sentryAndroidOptions.setDiagnosticLevel(SentryLevel.FATAL)

            sentryAndroidOptions.environment = if(BuildConfig.DEBUG) "debug" else "production"
        }

        TaloTracing.addTraceIdListener {
            setTag("taloId", it)
        }
    }
    }

    override fun log(msg: String) {
        if (!isAppDead()) {
      if(!msg.contains("do not log"))
        Sentry.captureMessage(msg)
    }
    }

    override fun dispatchThrowable(throwable: Throwable, hint: String?) {
    }

    override fun setTag(key: String, value: String?) {
        if (!isAppDead()) {
        if (value == null) {
            Sentry.removeTag(key)
        } else {
            Sentry.setTag(key, value)
        }
    }
    }

    override fun setContext(name: String, values: Map<String, Any>?) {
        if (!isAppDead()) {
        Sentry.configureScope { scope ->
            if (values == null) {
                scope.removeContexts(name)
            } else {
                scope.setContexts(name, values.toMap())
                }
            }
        }
    }
}