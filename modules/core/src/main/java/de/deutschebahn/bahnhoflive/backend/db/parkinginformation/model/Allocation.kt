package de.deutschebahn.bahnhoflive.backend.db.parkinginformation.model

class Allocation {

    var validData: Boolean? = null

    var timestamp: String? = null

    var timeSegment: String? = null

    var occupancy: Int? = null

    var vacancy: Int? = null

    var capacity: Int? = null

    var percentage: Int? = null

    var category: Int? = null

    var text: String? = null

    var belowThreshold: Boolean? = null

    var isUsedForPrognosis: Boolean? = null

}