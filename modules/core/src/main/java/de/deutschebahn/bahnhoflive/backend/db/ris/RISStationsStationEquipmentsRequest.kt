package de.deutschebahn.bahnhoflive.backend.db.ris

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Locker
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LockerDimension
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LockerFee
import de.deutschebahn.bahnhoflive.backend.parse
import org.json.JSONArray
import org.json.JSONObject

class RISStationsStationEquipmentsRequest(
    val stadaId: String,
    restListener: VolleyRestListener<List<Locker>>,
    dbAuthorizationTool: DbAuthorizationTool
) :
    RISStationsRequest<List<Locker>>(
        "station-equipments/locker/by-key?key=$stadaId&keyType=STATION_ID",
        dbAuthorizationTool,
        restListener
    ) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<List<Locker>>? {
        super.parseNetworkResponse(response)

        return parse(response) {

            val s = response.data.decodeToString()

            var lst: MutableList<Locker> = mutableListOf<Locker>()


            var ja: JSONArray = JSONObject(s).optJSONArray("lockerList")


            // todo parse correct
            s.let { body ->

                val ll: LinkedHashMap<String, Any?> = LinkedHashMap()

//                ll = JSONObject(body)

                JSONObject(body).optJSONArray("lockerList")?.run {

//                    asJSONObjectSequence().filterNotNull().mapNotNull { obj ->
//
//                        Log.d("cr", obj.toString())
//
//                    }

                    for (i in 0..this!!.length() - 1) {
//                        val x = this.getJSONObject(i).getString("size")
//                        Log.d("cr", x)

                    }


                    Log.d("cr", this.toString())
                }
            }

            lst.add(Locker().apply {
                amount = 1
                dimension = LockerDimension().apply {
                    depth = 25
                    width = 25
                    height = 25
                }
                fee = LockerFee().apply {
                    amount = 4
                    feePeriod = "24h"
                }

            })


            lst.add(Locker().apply { amount = 2 })

            Response.success(lst, HttpHeaderParser.parseCacheHeaders(response))
            lst

        }
    }

}