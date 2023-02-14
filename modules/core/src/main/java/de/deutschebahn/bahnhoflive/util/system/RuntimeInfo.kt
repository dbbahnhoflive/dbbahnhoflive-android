package de.deutschebahn.bahnhoflive.util.system

object RuntimeInfo {

    private val runtime: Runtime = Runtime.getRuntime()


    val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
    val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB

    fun getFreeHeapMemInBytes() : Long {
        val usedMemInB = (runtime.totalMemory() - runtime.freeMemory())
        val maxHeapSizeInB = runtime.maxMemory()
        val availHeapSizeInB = maxHeapSizeInB - usedMemInB

return         availHeapSizeInB
    }
}