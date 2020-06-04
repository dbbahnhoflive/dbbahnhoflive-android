package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

import com.google.gson.annotations.SerializedName
import java.util.regex.Pattern

class Neighbour {

    companion object {
        val evaLinkPattern = Pattern.compile(".*/(\\d+)")
    }

    var name: String? = null

    var belongsToStation: String? = null

    @SerializedName("_links")
    var links: Map<String, Link>? = null

    val link get() = links?.get(LinkKey.SELF)?.href

    val evaId
        get() = link?.let {
            evaLinkPattern.matcher(it).takeIf { it.matches() }?.group(1)
        }
}
