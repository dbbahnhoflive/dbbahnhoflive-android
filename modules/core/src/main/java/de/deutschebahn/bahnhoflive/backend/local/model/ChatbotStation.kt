/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.local.model

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.repository.Station
import java.util.*


private val String.isChatbotAvailable
    get() = ChatbotStation.isInTeaserPeriod && ChatbotStation.ids.contains(this)

val Station.isChatbotAvailable: Boolean
    get() = id.isChatbotAvailable

val DetailedStopPlace.isChatbotAvailable
    get() = stadaId.isChatbotAvailable == true

object ChatbotStation {

    val ids = setOf(
        "27", // Ahrensburg
        "2516", // Sternschanze
        "6859", // Wolfsburg Hbf
        "1059", // Coburg
        "1908", // Freising
        "5226", // Renningen (noch unklar, wird vll kein Zukunftsbahnhof sein)
        "2648", // Heilbronn Hbf
        "2498", // Halle (Saale) Hbf
        "6692", // Wernigerode
        "4280", // Münster Hbf (Anm. Maik: Münster (Westfalen))
        "2510", // Haltern am See
        "4859", // Berlin Südkreuz
        "791", // Berlin Bornholmer Straße
        "1077", // Cottbus
        "7171", // Offenbach Marktplatz
        "2827", // Hofheim (Anm. Maik: Hofheim(Taunus))
        "2514", // Hamburg Hbf
        "1866", // Frankfurt (Main) Hbf
        "4234", // München Hbf
        "3320", // Köln Hbf
        "1071", // Berlin Hbf
        "3631", // Leipzig Hbf
        "1401", // Düsseldorf Hbf
        "2545", // Hannover Hbf
        "527", // Berlin Friedrichstraße
        "4809", // Berlin Ostkreuz
        "393", // Bamberg
        "439", // Bayreuth Hbf
        "2818", // Hof Hbf
        "1059", // Coburg
        "3700", // Lichtenfels
        "2784", // Hirschaid
        "3979", // Marktredwitz
        "3458", // Kulmbach
        "3427", // Kronach
        "4384", // Neuenmarkt-Wirsberg
        "5963", // Bad Staffelstein
        "4444", // Neustadt (b Coburg)
        "3187", // Kirchenlaibach
        "4227", // Münchberg
        "1427", // Ebelsbach-Eltmann
        "5307", // Rödental
        "991", // Burgkunstadt
        "852", // Breitengüßbach
        "6065", // Strullendorf
        "1009", // Buttenheim
        "5302", // Bad Rodach (b Coburg)
        "5176", // Rehau
        "1442", // Ebersdorf (b Coburg)
        "4296", // Naila
        "1435", // Ebern
        "1479", // Eggolsheim
        "6983", // Zeil
        "6974", // Zapfendorf
        "8056", // Rödental Mitte
        "1431", // Ebensfeld
        "2810", // Hochstadt-Marktzeuln
        "4658", // Oberkotzau
        "3467", // Küps
        "1083", // Creußen (Oberfr)
        "4645", // Oberhaid
        "5028", // Pressig-Rothenkirchen
        "3844", // Ludwigsstadt
        "5989", // Steinbach a Wald
        "5811", // Selb Stadt
        "2506", // Hallstadt (b Bamberg)
        "3896", // Mainleus
        "431", // Baunach
        "5810", // Selb Nord
        "2819", // Hof-Neuhof
        "5158", // Reckendorf
        "2674", // Helmbrechts
        "351", // Bad Steben
        "5724", // Schwarzenbach (Saale)
        "5166", // Redwitz (Rodach)
        "8057", // Coburg Nord
        "6032", // Stockheim (Oberfr)
        "1270", // Dörfles-Esbach
        "4095", // Michelau (Oberfr)
        "2378", // Grub am Forst
        "3980", // Marktschorgast
        "2371", // Großwalbur
        "6366", // Untersteinach (b Stadtsteinach)
        "4167", // Mönchröden
        "1446", // Ebing
        "5630", // Schney
        "6244", // Trebgast
        "5812", // Selb-Plößberg
        "5573", // Schirnding
        "5835", // Seybothenreuth
        "648", // Bindlach
        "4437", // Neusorg
        "1840", // Förtschendorf
        "2564", // Harsdorf
        "5673", // Schönwald (Oberfr)
        "1769", // Feilitzsch
        "2984", // Immenreuth
        "6912", // Wunsiedel-Holenbrunn
        "4021", // Meeder
        "4436", // Neuses (b Kronach)
        "1082", // Creidlitz
        "5115", // Ramsenthal
        "6755", // Wiesenfeld (b Coburg)
        "3834", // Ludwigschorgast
        "5352", // Röslau
        "3976", // Marktleuthen
        "3990", // Martinlamitz
        "6483", // Waldershof
        "3306", // Köditz
        "186", // Arzberg (Oberfr)
        "5966", // Stammbach
        "1644", // Erkersreuth
        "3994", // Marxgrün
        "5229", // Rentweinsdorf
        "6029", // Stockau
        "5622", // Schnabelwaid
        "5813", // Selbitz
        "2414", // Gundelsdorf
        "3188", // Kirchenlamitz Ost
        "6955", // Wüstenselbitz
        "1060", // Coburg-Neuses
        "1828", // Förbau
        "3897", // Mainroth
        "2474", // Haidenaab-Göppmannsbühl
        "6941", // Wurlitz
        "5392", // Rothenbürg
        "2872", // Höllenthal
        "2761", // Hildbrandsgrün
        "3924", // Manndorf
        "5977", // Stegenwaldhaus
        "6331", // Unfriedsdorf
        "5833", // Seulbitz
        "11", // Adelebsen
        "54", // Alfeld (Leine)
        "56", // Algermissen
        "253", // Bad Bevensen
        "742", // Bad Bodenteich
        "279", // Bad Gandersheim
        "281", // Bad Harzburg
        "287", // Bad Karlshafen
        "8081", // Bad Lauterberg im Harz Barbis
        "326", // Bad Sachsa
        "328", // Bad Salzdetfurth
        "8064", // Bad Salzdetfurth-Solebad
        "369", // Baddeckenstedt
        "398", // Banteln
        "415", // Barnten
        "625", // Bienenbüttel
        "736", // Bodenburg
        "738", // Bodenfelde
        "798", // Börßum
        "835", // Braunschweig Hbf
        "836", // Braunschweig-Gliesmarode
        "891", // Brockhöfe
        "1022", // Calberlah
        "1069", // Coppenbrügge
        "1172", // Derneburg (Han)
        "1184", // Dettum
        "1448", // Ebstorf (Kr Uelzen)
        "1499", // Eichenberg
        "1520", // Einbeck-Salzderhelden
        "1577", // Elze (Han)
        "1586", // Emmerke
        "1759", // Fallersleben
        "1886", // Freden (Leine)
        "1915", // Frellstedt
        "1937", // Friedland (Han)
        "2107", // Gertenbach
        "2122", // Gifhorn
        "2123", // Gifhorn-Stadt
        "2129", // Gittelde
        "2202", // Goslar
        "2218", // Göttingen
        "2334", // Großdüngen
        "2543", // Hann Münden
        "2557", // Hardegsen
        "2565", // Harsum
        "2594", // Hattorf
        "2618", // Hedemünden
        "2677", // Helmstedt
        "2740", // Herzberg (Harz)
        "2742", // Herzberg Schloß
        "2765", // Hildesheim Hbf
        "2766", // Hildesheim Ost
        "2841", // Hoheneggelsen
        "2889", // Holzminden
        "3132", // Katlenburg
        "3295", // Knesebeck
        "3354", // Königslutter
        "3407", // Kreiensen
        "3523", // Langelsheim
        "3582", // Lauenförde
        "3622", // Leiferde (b Gifhorn)
        "894", // Lengede-Broistedt
        "8085", // Lenglern
        "3760", // Lödingsen
        "4028", // Meine
        "4031", // Meinersen
        "4229", // Münchehof (Harz)
        "4279", // Munster (Örtze)
        "4374", // Neudorf-Platendorf
        "4581", // Nordstemmen
        "4586", // Nörten-Hardenberg
        "4587", // Northeim (Han)
        "4746", // Offensen (Kr Northeim)
        "4756", // Oker
        "8087", // Osterode am Harz Leege
        "8086", // Osterode am Harz Mitte
        "4802", // Osterwald
        "4885", // Peine
        "5384", // Rötgesbüttel
        "5478", // Salzgitter Bad
        "5479", // Salzgitter-Immendorf
        "5480", // Salzgitter-Lebenstedt
        "5481", // Salzgitter-Ringelheim
        "5482", // Salzgitter-Thiede
        "5483", // Salzgitter-Watenstedt
        "5510", // Sarstedt
        "5540", // Schandelah
        "5579", // Schladen (Harz)
        "8010", // Schnega
        "5656", // Schönewörde
        "5681", // Schöppenstedt
        "5797", // Seesen
        "8011", // Soltendieck
        "5958", // Stadtoldendorf
        "5975", // Stederdorf (Kr Uelzen)
        "6095", // Suderburg
        "6257", // Triangel
        "6310", // Uelzen
        "6380", // Uslar
        "6397", // Vechelde
        "6409", // Vernawahlshausen
        "6412", // Vienenburg
        "6440", // Vöhrum
        "6444", // Voldagsen
        "6450", // Volpriehausen
        "6452", // Vorhop
        "6470", // Wahrenholz
        "6499", // Walkenried
        "6574", // Weddel (Braunschw)
        "8065", // Wesseln
        "6741", // Wieren
        "6833", // Wittingen
        "6838", // Witzenhausen Nord
        "6850", // Wolfenbüttel
        "6859", // Wolfsburg Hbf
        "6879", // Woltwiesche
        "6904" // Wulften
    )

    private val teaserPeriodStart by lazy {
        createDateCalendar(2019, Calendar.NOVEMBER, 18)
    }

    /**
     * First day of inactive period.
     */
    private val teaserPeriodEnd by lazy {
        createDateCalendar(2021, Calendar.JANUARY, 1)
    }

    private fun createDateCalendar(year: Int, month: Int, dayOfMonth: Int) =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    val isInTeaserPeriod: Boolean
        get() =
            Calendar.getInstance().run {
                before(teaserPeriodEnd) && after(teaserPeriodStart)
            }
}