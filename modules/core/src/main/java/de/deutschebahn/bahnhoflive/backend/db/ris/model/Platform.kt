package de.deutschebahn.bahnhoflive.backend.db.ris.model

import android.content.Context
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import java.text.Collator
import java.util.*
import kotlin.math.abs


class Platform(
    val name: String,
    val accessibility: EnumMap<AccessibilityFeature, AccessibilityStatus>,
    val linkedPlatforms : MutableList<String>? = null,
    val isHeadPlatform : Boolean,
    val start : Double,
    val end : Double,
    val length : Double,
    var level : Int = UNKNOWN_LEVEL // wird erst bei Bedarf gesetzt

) : Comparable<Platform> {

    companion object {
        const val UNKNOWN_LEVEL = 100
        val numberPattern = Regex("\\d+")

        val collator = Collator.getInstance(Locale.GERMAN)

        fun platformNumber(platformString: String?, defaultValue: Int = 0): Int {

            if(platformString==null) return defaultValue

            val ret = runCatching {
                numberPattern.find(platformString)?.value?.toInt()
            }.getOrNull()

            return ret ?: defaultValue
        }

        fun staticLevelToText(context: Context, level:Int, fullText: Boolean = false): String {
            (abs(level)).let {
                if (fullText) {
                    when {
                        level < 0 -> return context.resources.getString(
                            R.string.template_level_underground, it
                        )

                        level == UNKNOWN_LEVEL -> return context.resources.getString(R.string.level_unknown)
                        level > 0 -> return context.resources.getString(R.string.template_level_overground, it)
                        else -> return context.resources.getString(R.string.level_base_shortcut)
                    }
                } else {
                    when {
                        level < 0 -> return context.resources.getString(R.string.template_level_underground_shortcut, it)
                        level == UNKNOWN_LEVEL -> return context.resources.getString(R.string.level_unknown)
                        level > 0 -> return context.resources.getString(R.string.template_level_overground_shortcut, it)
                        else -> return context.resources.getString(R.string.level_base_shortcut)
                    }
                }
            }
        }
    }

    val number : Int? = runCatching {
        numberPattern.find(name)?.value?.toInt()
    }.getOrNull()

    fun levelToText(context: Context, fullText: Boolean = false): String = staticLevelToText(context, level, fullText)

    fun formatLinkedPlatformString(includePlatform: Boolean = true) : String {

        val platformAsInt = this.number ?: 0

        val arrangedPlatforms: MutableList<Int> =
            linkedPlatforms?.map { it.toInt() }?.toMutableList() ?: mutableListOf()

        if (includePlatform)
            if (!arrangedPlatforms.contains(platformAsInt))
                arrangedPlatforms.add(0, platformAsInt)

        var s = ""

        arrangedPlatforms.indices.forEach {
            if (s != "")
                s += " | "
            s += arrangedPlatforms[it]
        }

        return s
    }

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


//fun List<Platform>.findLinkedPlatformNumber(platform:String) : Int? {
//    val platformNumber : Int? = kotlin.runCatching { Platform.platformNumber(platform) }.getOrNull()
//
//    if(platformNumber==null || platformNumber==0)
//        return null
//
//    val linkedNr : Int? = this.filter { it.number == platformNumber }.firstOrNull()?.linkedPlatformNumber()
//    if(linkedNr!=null) return linkedNr
//
//    // eventuell fehler im api, suchen, ob ein anderes gleis auf dieses verweist
//    return this.filter { it.linkedPlatformNumber() == platformNumber }.firstOrNull()?.number
//}

fun List<Platform>.findPlatform(platformName:String) : Platform? {
    val platformNumber : Int = Platform.platformNumber(platformName)

    (platformNumber>0).let {
        return this.firstOrNull { it.number == platformNumber }
    }

    return null
}

//fun List<Platform>.contains(platformName:String, includeLinkedPlatform:Boolean=true) : Boolean {
//    val platformNumber : Int = Platform.platformNumber(platformName)
//    this.forEach {if(it.number==platformNumber || (if(includeLinkedPlatform) it.linkedPlatformNumber()==platformNumber else false)) return true}
//    return false
//}

fun List<Platform>.countLinkedPlatforms(platformName:String) : Int {
    val platformNumber : Int = Platform.platformNumber(platformName)

    var n = 0
    this.forEach {
        it.linkedPlatforms?.forEach {
            if(Platform.platformNumber(it) == platformNumber)
                n++
        }
    }
    return n
}


fun List<Platform>.findLevel(platformName:String) : Int {
    val platformNumber : Int = Platform.platformNumber(platformName)

    (platformNumber>0).let {
        return this.first { it.number == platformNumber }.level
    }

    return Platform.UNKNOWN_LEVEL
}

