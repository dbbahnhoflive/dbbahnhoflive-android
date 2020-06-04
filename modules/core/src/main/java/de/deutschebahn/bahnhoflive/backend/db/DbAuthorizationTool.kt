package de.deutschebahn.bahnhoflive.backend.db

open class DbAuthorizationTool(
    protected val apiKey: String
) {

    open val key get() = apiKey

    fun putAuthorizationHeader(headers: MutableMap<String, String>?): Map<String, String> {
        var headers = headers
        if (headers == null || headers == emptyMap<Any, Any>()) {
            headers = HashMap()
        }
        headers["key"] = key

        return headers
    }

}