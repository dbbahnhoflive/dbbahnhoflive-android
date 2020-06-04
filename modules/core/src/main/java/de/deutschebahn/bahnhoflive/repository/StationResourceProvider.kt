package de.deutschebahn.bahnhoflive.repository

interface StationResourceProvider {
    fun getStationResource(id: String): StationResource
}