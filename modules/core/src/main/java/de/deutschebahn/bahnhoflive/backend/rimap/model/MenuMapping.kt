package de.deutschebahn.bahnhoflive.backend.rimap.model

import de.deutschebahn.bahnhoflive.ui.station.shop.ShopCategory

object MenuMapping {
    const val DB_INFORMATION = "DB_INFORMATION"
    const val DB_TRAVEL_CENTER = "DB_TRAVEL_CENTER"
    const val TICKETS = "TICKETS"
    const val TOURIST_INFORMATION = "TOURIST_INFORMATION"

    /**
     * 2021-09-02: Finally replaced by [PLATFORM]. Keep it in code for a while just to make sure...
     */
    @Deprecated("Replaced by PLATFORM")
    const val PLATFROM = "PLATFROM"

    /**
     * Note that as of 2021-07-07 Rimap is still using the wrong spelling [PLATFROM]
     */
    const val PLATFORM = "PLATFORM"
    val PLATFORM_TYPES = listOf(PLATFORM, PLATFROM)

    const val PLATFORM_SECTOR_CUBE = "PLATFORM_SECTOR_CUBE"
    const val AIRPORT = "AIRPORT"
    const val COACH = "COACH"
    const val FERRY = "FERRY"
    const val CITY_TRAIN = "CITY_TRAIN"
    const val SUBWAY = "SUBWAY"
    const val BUS = "BUS"
    const val TRAM = "TRAM"
    const val RAIL_REPLACEMENT_TRANSPORT = "RAIL_REPLACEMENT_TRANSPORT"
    const val TAXI = "TAXI"
    const val FLINKSTER = "FLINKSTER"
    const val CARSHARING = "CARSHARING"
    const val RENTAL_CAR = "RENTAL_CAR"
    const val PARKING_AREA = "PARKING_AREA"
    const val PARKING_DECK = "PARKING_DECK"
    const val CALL_A_BIKE = "CALL_A_BIKE"
    const val BIKE_RENTAL = "BIKE_RENTAL"
    const val BIKE_PARKING_AREA = "BIKE_PARKING_AREA"
    const val BIKE_STATION = "BIKE_STATION"
    const val TOILET = "TOILET"
    const val TOILET_HANDICAPPED = "TOILET_HANDICAPPED"
    const val WAITING_AREA = "WAITING_AREA"
    const val LOCKER = "LOCKER"
    const val BAGGAGE_ROOM = "BAGGAGE_ROOM"
    const val WIFI = "WIFI"
    const val LETTERBOX = "LETTERBOX"
    const val CASHPOINT = "CASHPOINT"
    const val RAILWAY_MISSION = "RAILWAY_MISSION"
    const val FEDERAL_POLICE = "FEDERAL_POLICE"
    const val LOST_AND_FOUND = "LOST_AND_FOUND"
    const val DB_LOUNGE = "DB_LOUNGE"
    const val FOOD = "FOOD"
    const val RESTAURANT = "RESTAURANT"
    const val COFFEE_SHOP = "COFFEE_SHOP"
    const val FAST_FOOD = "FAST_FOOD"
    const val BAKERY = "BAKERY"
    const val SUPERMARKET = "SUPERMARKET"
    const val INN = "INN"
    const val SHOPPING_COMMON = "SHOPPING_COMMON"
    const val HEALTH = "HEALTH"
    const val FLOWERS = "FLOWERS"
    const val PRESS = "PRESS"
    const val FASHION = "FASHION"
    const val PHARMACY = "PHARMACY"
    const val SERVICE_COMMON = "SERVICE_COMMON"
    const val HOTEL = "HOTEL"
    const val TRAVEL_AGENCY = "TRAVEL_AGENCY"
    const val POST = "POST"
    const val FINANCIAL_INSTITUTE = "FINANCIAL_INSTITUTE"
    const val ENTERTAINMENT = "ENTERTAINMENT"
    const val STAIR = "STAIR"
    const val ESCALATOR = "ESCALATOR"
    const val ELEVATOR = "ELEVATOR"
    const val RAMP = "RAMP"
    const val ENTRANCE_EXIT = "ENTRANCE_EXIT"

    val mapping = mapOf(
        DB_INFORMATION to (ShopCategory.Menucat.TICKETSREISEAUSKUNFT to "DB Information"),
        DB_TRAVEL_CENTER to (ShopCategory.Menucat.TICKETSREISEAUSKUNFT to "DB Reisezentrum"),
        TICKETS to (ShopCategory.Menucat.TICKETSREISEAUSKUNFT to "Fahrkarten"),
        TOURIST_INFORMATION to (ShopCategory.Menucat.TICKETSREISEAUSKUNFT to "Touristeninformation"),
        PLATFROM to (ShopCategory.Menucat.BAHNGLEISEFERNVERKEHR to "Bahngleise"),
        PLATFORM to (ShopCategory.Menucat.BAHNGLEISEFERNVERKEHR to "Bahngleise"),
        PLATFORM_SECTOR_CUBE to (ShopCategory.Menucat.BAHNGLEISEFERNVERKEHR to "Abschnittswürfel"),
        AIRPORT to (ShopCategory.Menucat.BAHNGLEISEFERNVERKEHR to "Flughafen"),
        COACH to (ShopCategory.Menucat.BAHNGLEISEFERNVERKEHR to "Fernbus"),
        FERRY to (ShopCategory.Menucat.BAHNGLEISEFERNVERKEHR to "Fähre"),
        CITY_TRAIN to (ShopCategory.Menucat.OEPNV to "S-Bahn"),
        SUBWAY to (ShopCategory.Menucat.OEPNV to "U-Bahn"),
        BUS to (ShopCategory.Menucat.OEPNV to "Bus"),
        TRAM to (ShopCategory.Menucat.OEPNV to "Tram"),
        RAIL_REPLACEMENT_TRANSPORT to (ShopCategory.Menucat.OEPNV to "SEV"),
        TAXI to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Taxi"),
        FLINKSTER to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Flinkster"),
        CARSHARING to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Carsharing"),
        RENTAL_CAR to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Mietwagen"),
        PARKING_AREA to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Parkplatz"),
        PARKING_DECK to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Parkhaus"),
        CALL_A_BIKE to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Call a Bike"),
        BIKE_RENTAL to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Fahrradverleih"),
        BIKE_PARKING_AREA to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Fahrradparkplatz"),
        BIKE_STATION to (ShopCategory.Menucat.INDIVIDUALTRAFFIC to "Radstation"),
        TOILET to (ShopCategory.Menucat.STATIONSERVICES to "WC"),
        TOILET_HANDICAPPED to (ShopCategory.Menucat.STATIONSERVICES to "WC Rollstuhlbenutzer"),
        WAITING_AREA to (ShopCategory.Menucat.STATIONSERVICES to "Wartebereich"),
        LOCKER to (ShopCategory.Menucat.STATIONSERVICES to "Schließfach"),
        BAGGAGE_ROOM to (ShopCategory.Menucat.STATIONSERVICES to "Gepäckaufbewahrung"),
        WIFI to (ShopCategory.Menucat.STATIONSERVICES to "WLAN"),
        LETTERBOX to (ShopCategory.Menucat.STATIONSERVICES to "Briefkasten"),
        CASHPOINT to (ShopCategory.Menucat.STATIONSERVICES to "EC-Geldautomat"),
        RAILWAY_MISSION to (ShopCategory.Menucat.STATIONSERVICES to "Bahnhofsmission"),
        FEDERAL_POLICE to (ShopCategory.Menucat.STATIONSERVICES to "Bundespolizei"),
        LOST_AND_FOUND to (ShopCategory.Menucat.STATIONSERVICES to "Fundbüro"),
        DB_LOUNGE to (ShopCategory.Menucat.STATIONSERVICES to "DB Lounge"),
        FOOD to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Lebensmittel"),
        RESTAURANT to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Restaurant"),
        COFFEE_SHOP to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Café"),
        FAST_FOOD to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Fast Food"),
        BAKERY to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Bäckerei"),
        SUPERMARKET to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Supermarkt"),
        INN to (ShopCategory.Menucat.GASTRONOMIE_LEBENSMITTEL to "Gaststätte"),
        SHOPPING_COMMON to (ShopCategory.Menucat.EINKAUFEN to "Einkaufen"),
        HEALTH to (ShopCategory.Menucat.EINKAUFEN to "Gesundheit"),
        FLOWERS to (ShopCategory.Menucat.EINKAUFEN to "Blumen"),
        PRESS to (ShopCategory.Menucat.EINKAUFEN to "Presse"),
        FASHION to (ShopCategory.Menucat.EINKAUFEN to "Mode"),
        PHARMACY to (ShopCategory.Menucat.EINKAUFEN to "Apotheke"),
        SERVICE_COMMON to (ShopCategory.Menucat.DIENSTLEISTUNGEN to "Dienstleistungen"),
        HOTEL to (ShopCategory.Menucat.DIENSTLEISTUNGEN to "Hotel"),
        TRAVEL_AGENCY to (ShopCategory.Menucat.DIENSTLEISTUNGEN to "Reisebüro"),
        POST to (ShopCategory.Menucat.DIENSTLEISTUNGEN to "Post"),
        FINANCIAL_INSTITUTE to (ShopCategory.Menucat.DIENSTLEISTUNGEN to "Geldinstitut"),
        ENTERTAINMENT to (ShopCategory.Menucat.DIENSTLEISTUNGEN to "Unterhaltung"),
        STAIR to (ShopCategory.Menucat.ROUTING to "Treppen"),
        ESCALATOR to (ShopCategory.Menucat.ROUTING to "Fahrtreppen"),
        ELEVATOR to (ShopCategory.Menucat.ROUTING to "Aufzüge"),
        RAMP to (ShopCategory.Menucat.ROUTING to "Rampen"),
        ENTRANCE_EXIT to (ShopCategory.Menucat.ROUTING to "Ein-/Ausgänge")
    )
}