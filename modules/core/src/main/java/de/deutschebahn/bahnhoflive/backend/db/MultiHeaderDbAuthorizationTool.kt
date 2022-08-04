package de.deutschebahn.bahnhoflive.backend.db

import de.deutschebahn.bahnhoflive.util.asMutable

open class MultiHeaderDbAuthorizationTool(
    protected val headerFields: Map<String, String>,
) : DbAuthorizationTool(
    headerFields.getOrElse(HEADER_API_KEY) { "" }
) {

    constructor(apiKey: String, clientId: String) : this(
        mapOf(
            HEADER_API_KEY to apiKey,
            HEADER_CLIENT_ID to clientId
        )
    )

    override val key: String
        get() = throw UnsupportedOperationException()

    override fun putAuthorizationHeader(
        headers: Map<String, String>?,
        keyName: String
    ): Map<String, String> = (headers?.asMutable() ?: mutableMapOf()).apply {
        putAll(headerFields)
    }

    companion object {
        const val HEADER_API_KEY = "db-api-key"
        const val HEADER_CLIENT_ID = "db-client-id"
    }

}