package de.deutschebahn.bahnhoflive.backend.db.ris.model

import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import java.text.Collator
import java.util.*

fun List<Platform>.findLinkedPlatform(platform:String) : Int? {
    val platformNumber : Int? = kotlin.runCatching { platform.toInt() }.getOrNull()

    platformNumber?.let {
        return this.filter { it.number == platformNumber }.firstOrNull()?.linkedPlatformNumber
    }

    return null
}


class Platform(
    val name: String,
    val accessibility: EnumMap<AccessibilityFeature, AccessibilityStatus>,
    private val linkedPlatform : String?=null
) : Comparable<Platform> {

    companion object {
        val numberPattern = Regex("\\d+")

        val collator = Collator.getInstance(Locale.GERMAN)

        fun platformNumber(platformString: String, defaultValue: Int = 0): Int {
            val ret = runCatching {
                numberPattern.find(platformString)?.value?.toInt()
            }.getOrNull()

            return ret ?: defaultValue
        }
    }

    val number : Int? = runCatching {
        numberPattern.find(name)?.value?.toInt()
    }.getOrNull()

    val linkedPlatformNumber : Int? = linkedPlatform?.runCatching {
        numberPattern.find(linkedPlatform)?.value?.toInt()
    }?.getOrNull()

    override fun compareTo(other: Platform): Int =
        if (number != null && other.number != null) {
            when (val numericalDifference = number - other.number) {
                0 -> collator.compare(name, other.name)
                else -> numericalDifference
            }
        } else {
            collator.compare(name, other.name)
        }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Platform -> false
        name != other.name -> false
        else -> true
    }

    override fun hashCode(): Int = name.hashCode()

}