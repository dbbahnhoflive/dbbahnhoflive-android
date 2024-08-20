package de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model

import java.util.Objects

/*
{
  "administrationID": "800643",
  "operatorCode": "DB",
  "operatorName": "DB Regio AG S-Bahn Stuttgart"
},
*/

class RisAdminWagonOrder
{
  val  administrationID : String = ""
  val  operatorCode : String = ""
  val  operatorName : String = ""

  override fun hashCode(): Int {
    return Objects.hash(administrationID);
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return this.hashCode()==(other as? RisAdminWagonOrders).hashCode()
  }
}

class RisAdminWagonOrders
{
  private val administrations : MutableList<RisAdminWagonOrder> = mutableListOf()

  fun containsAdministrationID(id:String?) : Boolean {
    if(id==null) return false
    return administrations.firstOrNull { it.administrationID==id } !=null
  }

  fun replace(newList: RisAdminWagonOrders) {

    val hash1 = administrations.hashCode()
    val hash2 = newList.administrations.hashCode()

    if(hash1!=hash2) {
      administrations.clear()
      administrations.addAll(newList.administrations)
    }
  }
}