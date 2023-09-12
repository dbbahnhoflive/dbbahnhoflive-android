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
    private val linkedPlatforms : MutableList<String>? = null,
    val isHeadPlatform : Boolean,
    val start : Double,
    val end : Double,
    val length : Double,
    var level : Int = LEVEL_UNKNOWN // wird erst bei Bedarf gesetzt

) : Comparable<Platform> {

    companion object {
        const val LEVEL_UNKNOWN = 100
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

                        level == LEVEL_UNKNOWN -> return context.resources.getString(R.string.level_unknown)
                        level > 0 -> return context.resources.getString(R.string.template_level_overground, it)
                        else -> return context.resources.getString(R.string.level_base_shortcut)
                    }
                } else {
                    when {
                        level < 0 -> return context.resources.getString(R.string.template_level_underground_shortcut, it)
                        level == LEVEL_UNKNOWN -> return context.resources.getString(R.string.level_unknown)
                        level > 0 -> return context.resources.getString(R.string.template_level_overground_shortcut, it)
                        else -> return context.resources.getString(R.string.level_base_shortcut)
                    }
                }
            }
        }
    }

    val hasLinkedPlatforms : Boolean
        get() = linkedPlatforms!=null
    val linkedPlatformNumbers : MutableList<Int>
        get() {
           val fullmap = linkedPlatforms?.map { platformNumber(it) }?.toMutableList() ?: mutableListOf()

           val resultMap : MutableList<Int> = mutableListOf()

            val iter = fullmap.iterator()
            while(iter.hasNext()) {
              val value = iter.next()
              if(!resultMap.contains(value) && value!=number)
                  resultMap.add(value)
            }
            return resultMap
        }

    val number : Int? = runCatching {
        numberPattern.find(name)?.value?.toInt()
    }.getOrNull()

    fun levelToText(context: Context, fullText: Boolean = false): String = staticLevelToText(context, level, fullText)

    fun formatLinkedPlatformString(includePlatform: Boolean = true, sort:Boolean=true) : String {

        val platformAsInt = this.number ?: 0

        val tmpLinkedPlatformsAsInts  = linkedPlatformNumbers


        if (includePlatform)
            if (!tmpLinkedPlatformsAsInts.contains(platformAsInt))
                tmpLinkedPlatformsAsInts.add(0, platformAsInt)

        if(sort)
            tmpLinkedPlatformsAsInts.sort()

        var s = ""

        tmpLinkedPlatformsAsInts.indices.forEach {
            if (s != "")
                s += " | "
            s += tmpLinkedPlatformsAsInts[it]
        }

        return s
    }

    override fun compareTo(other: Platform): Int =
        if (number != null && other.number != null) {
            when (val numericalDifference = number - other.number) {
                0 -> {
                   val result = collator.compare(name, other.name)
                   if(result==0) {
                       if (linkedPlatforms == null)
                            1
                       else
                           -1
                   }
                   else
                    result
                }
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

fun List<Platform>.findPlatform(platformName:String) : Platform? {
    val platformNumber : Int = Platform.platformNumber(platformName)

    if(platformNumber>0) {
        return this.firstOrNull { it.number == platformNumber }
    }

    return null
}

fun List<Platform>.hasLinkedPlatform(platformNumber:Int?) : Boolean {
    this.forEach {
        if (it.number != platformNumber) {
            if (it.linkedPlatformNumbers.contains(platformNumber)) return true
        }
    }
    return false
}

fun List<Platform>.containsPlatform(platformNumber:Int?) : Boolean {
    this.forEach {
        if (it.number == platformNumber) {
            return true
    }
    }
    return false
}
