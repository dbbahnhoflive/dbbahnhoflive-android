package de.deutschebahn.bahnhoflive.backend.db.ris.model

import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import java.text.Collator
import java.util.*

class Platform(
    val name: String,
    val accessibility: EnumMap<AccessibilityFeature, AccessibilityStatus>
) : Comparable<Platform> {

    companion object {
        val numberPattern = Regex("\\d+")

        val collator = Collator.getInstance(Locale.GERMAN)
    }

    protected val number = runCatching {
        numberPattern.find(name)?.value?.toInt()
    }.getOrNull()

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