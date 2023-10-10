package de.deutschebahn.bahnhoflive.util.accessibility

object AccessibilityUtilities {


    // 6:5 -> sechs Uhr 5
    // 18:00 -> "Achtzehn Uhr"
    // 18:05 -> "Achtzehn Uhr fünf"
    // 18:15 -> "Achtzehn Uhr fünfzehn"


    private fun getSpokenMinute( minute:Int, baseTime:String) : String {

        return when(minute) {
            1 -> "ein"  + " und " + baseTime
            2 -> "zwei" + " und " + baseTime
            3 -> "drei" + " und " + baseTime
            4 -> "vier" + " und " + baseTime
            5 -> "fünf" + " und " + baseTime
            6 -> "sechs" + " und " + baseTime
            7 -> "sieben" + " und " + baseTime
            8 -> "acht" + " und " + baseTime
            9 -> "neun" + " und " + baseTime
            else -> baseTime
        }

    }

    fun getSpokenTime(time:String):String {

        var result=time

        try {

            val items = time.split(":")

            if (items.size != 2)
                return time

            val hour = items[0].toInt()
            val minute = items[1].toInt()

            val hourString = when (hour) {
                0->"null"
                1->"ein"
                2->"zwei"
                3->"drei"
                4->"vier"
                5->"fünf"
                6->"sechs"
                7->"sieben"
                8->"acht"
                9->"neun"
                10->"zehn"
                11->"elf"
                12->"zwölf"
                13->"dreizehn"
                14->"vierzehn"
                15->"fünfzehn"
                16->"sechszehn"
                17->"siebzehn"
                18->"achtzehn"
                19->"neunzehn"
                20->"zwanzig"
                21->"einundzwanzig"
                22->"zweiundzwanzig"
                23->"dreiundzwanzig"
             else->"null"
            }


            val minuteString = when (minute) {

                0->""
                1->"eins"
                2->"zwei"
                3->"drei"
                4->"vier"
                5->"fünf"
                6->"sechs"
                7->"sieben"
                8->"acht"
                9->"neun"

                10->"zehn"
                11->"elf"
                12->"zwölf"
                13->"dreizehn"
                14->"vierzehn"
                15->"fünfzehn"
                16->"sechszehn"
                17->"siebzehn"
                18->"achtzehn"
                19->"neunzehn"

                in 20..29-> getSpokenMinute(minute%10, "zwanzig")

                in 30..39-> getSpokenMinute(minute%10, "dreissig")

                in 40..49-> getSpokenMinute(minute%10,"vierzig")

                in 50..59-> getSpokenMinute(minute%10, "fünfzig")

                else -> ""
            }

            result = "${hourString} Uhr ${minuteString}"



        }
        catch(_:Exception) {
        }

        return result

    }

    fun getSpokenTime(time:CharSequence):String {
        return getSpokenTime(time.toString())
    }


}