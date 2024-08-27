package de.deutschebahn.bahnhoflive.backend.db.ris.model

import android.content.Context
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import java.text.Collator
import java.util.EnumMap
import java.util.EnumSet
import java.util.Locale
import java.util.TreeSet
import kotlin.math.abs

typealias PlatformList = List<Platform>
typealias PlatformName = String

class PlatformWithLevelAndLinkedPlatforms (val level:Int, val platformName:PlatformName, var linkedPlatforms:MutableSet<PlatformName>)

class PlatformWithLevelAndLinkedPlatformsComparator : Comparator<PlatformWithLevelAndLinkedPlatforms> {
    override fun compare(
        o1: PlatformWithLevelAndLinkedPlatforms,
        o2: PlatformWithLevelAndLinkedPlatforms
    ): Int {
        return TrackComparator().compare(o1.platformName, o2.platformName)
    }
}

open class TrackComparator : Comparator<PlatformName?> {

    private fun extractDigits(src: String): String {
        val builder = StringBuilder()
        for (i in 0 until src.length) {
            val c = src[i]
            if (Character.isDigit(c)) {
                builder.append(c)
            }
            else
                break
        }
        return builder.toString()
    }
    override fun compare(o1: PlatformName?, o2: PlatformName?): Int {
        //comparing tracks which might contain single numbers, but also combination of numbers and chars or chars only, e.g. "k.a.", "7 A-D"

        if(o1==null)
            return 1
        if(o2==null)
            return -1


        try {
            //try to convert strings into numbers and compare them
            val d1 = extractDigits(o1)
            val d2 = extractDigits(o2)

            if(d1.isEmpty())
                return 1
            if(d2.isEmpty())
                return -1

            if (d1.isNotEmpty() && d2.isNotEmpty()) {
                val res = Integer.valueOf(d1).compareTo(Integer.valueOf(d2))
                return if (res == 0) {
                    //extracted digits are equal, compare original strings
                    o1.compareTo(o2)
                } else {
                    res
                }
            }
        } catch (e: Exception) {
            //ignore and try string compare
        }
        return o1.compareTo(o2)
    }
}


class Platform(
    val name: String,
    val accessibility: EnumMap<AccessibilityFeature, AccessibilityStatus>,
    val linkedPlatforms : MutableList<String>? = null,
    val isHeadPlatform : Boolean,
    var level : Int = LEVEL_UNKNOWN // wird erst bei Bedarf gesetzt
) : Comparable<Platform> {

    companion object {
        const val LEVEL_UNKNOWN = 100
        val numberPattern = Regex("\\d+")

        val collator : Collator = Collator.getInstance(Locale.GERMAN)?:Collator.getInstance()
        fun platformNumber(platformString: String?, defaultValue: Int = 0): Int {

            if(platformString==null) return defaultValue

            val ret = runCatching {
                numberPattern.find(platformString)?.value?.toInt()
            }.getOrNull()

            return ret ?: defaultValue
        }

        fun staticLevelToText(context: Context, level: Int): String {
            (abs(level)).let {
                    when {
                        level < 0 -> return context.resources.getString(
                            R.string.template_level_underground, it
                        )
                        level == LEVEL_UNKNOWN -> return context.resources.getString(R.string.level_unknown)
                    level > 0 -> return context.resources.getString(
                        R.string.template_level_overground,
                        it
                    )
                    else -> return context.resources.getString(R.string.level_base)
                }
            }

        }

        }

    val hasLevel: Boolean
        get() = level!=LEVEL_UNKNOWN

    val hasLinkedPlatforms : Boolean
        get() = linkedPlatforms!=null

    val hasAccessibilityInformation: Boolean
        get() = accessibility != EnumMap(
            EnumSet.allOf(AccessibilityFeature::class.java)
                .associateWith { AccessibilityStatus.UNKNOWN })

    val countLinkedPlatforms : Int
        get() = linkedPlatforms?.size ?: 0


    val number : Int? = runCatching {
        numberPattern.find(name)?.value?.toInt()
    }.getOrNull()

    fun levelToText(context: Context): String = staticLevelToText(context, level)

    fun formatLinkedPlatformString() : String {

        var s = ""

        linkedPlatforms?.indices?.forEach {
            if (s != "")
                s += " | "
            s += linkedPlatforms[it]
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

    fun hasAccessibilty() = accessibility.filter { it.value!=AccessibilityStatus.UNKNOWN }.isNotEmpty()
}

fun List<Platform>.findPlatform(platformName:String) : Platform? {
  return this.firstOrNull { it.name == platformName }
}

fun List<Platform>.containsPlatform(platformName:String?) : Boolean {
    this.forEach {
        if (it.name == platformName) {
            return true
        }
    }
    return false
}

fun List<Platform>.removeNotExistingLinkedPlatforms() {

    this.forEach { itLoopItem ->

        itLoopItem.linkedPlatforms?.let { itLinkedPlatforms ->
            run {

                val iter = itLinkedPlatforms.iterator()

                while (iter.hasNext()) {

                    val item = iter.next()
                    if (!this.containsPlatform(item))
                        iter.remove()

                }

            }
        }
    }
}

fun List<Platform>.getPlatformWithMostLinkedPlatforms(platformName:String?) : Platform? {
    var ret : Platform? = null
    this.forEach {itLoopItem->
        if (itLoopItem.name == platformName) {

            if(ret==null)
                ret = itLoopItem // null
            else
                if(itLoopItem.countLinkedPlatforms > ret!!.countLinkedPlatforms)
                    ret = itLoopItem

        }
    }
    return ret
}

fun Platform.combineToSet(includeLinkedPlatforms: Boolean=true) : TreeSet<PlatformName> {
    val linkedPlatformSet : TreeSet<PlatformName> =  TreeSet<PlatformName>(TrackComparator())
    linkedPlatformSet.add(name)
    if(includeLinkedPlatforms) {
        this.linkedPlatforms?.let {
            linkedPlatformSet.addAll(it)
        }
    }
    return linkedPlatformSet
}

fun List<Platform>.firstLinkedPlatform(platformName:String?) : Platform? {
    this.forEach { itPlatform ->
        if (itPlatform.name == platformName) {
            itPlatform.linkedPlatforms?.let {itPlatformNames->
                return if (itPlatformNames.size>0)
                    this.firstOrNull { itPlatformNames[0] == it.name }
            else
                null
            }
        }
    }
    return null
}

/**
 * find level
 * 1. search in existing platforms
 * 2. search in linked platforms
 */
fun List<Platform>.getLevel(
    platformName: String,
    poiLevelList: List<Pair<String, Int>>? = null
): Int {
    this.forEach { itPlatform ->
        if (itPlatform.name == platformName) {

            if (itPlatform.hasLevel)
                return itPlatform.level

            itPlatform.linkedPlatforms?.forEach { itName ->
                poiLevelList?.firstOrNull { it.first == itName }?.let {
                    return it.second
                }

            }
        }
    }

    this.forEach { itPlatform ->
        itPlatform.linkedPlatforms?.let { itLinkedPlatformNames ->

            if (itLinkedPlatformNames.contains(platformName)) {
                for (item in itLinkedPlatformNames) {
                    this.findPlatform(item)?.let {
                        if (it.hasLevel)
                            return it.level
                    }
                }

            }


        }
    }

    return Platform.LEVEL_UNKNOWN
}

