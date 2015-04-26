package com.nilson.controllers

import com.nilson.models._
import com.nilson.process.GameData
import play.api.libs.json.{Writes, Json}
import play.api.mvc._

/**
 *
 * @author Nilson
 * @since 3/31/2015
 */
object Data extends Controller {

  implicit val attackWrites = new Writes[Attack] {
    def writes(attack: Attack) = {
      Json.obj(
        "guns" -> attack.guns,
        "range" -> attack.range,
        "damage" -> attack.finalDamage,
        "targeting" -> attack.targeting,
        "rateOfFire" -> attack.rateOfFire,
        "critChance" -> attack.critChance,
        "averageDamage" -> attack.averageDamage
      )
    }
  }
  implicit val defenseWrites = new Writes[Defense] {
    def writes(defense: Defense) = {
      Json.obj(
        "hull" -> defense.finalHull,
        "absorbs" -> defense.absorbs,
        "shield" -> defense.finalShield,
        "shieldRecharge" -> defense.shieldRecharge,
        "maneuverability" -> defense.maneuverability
      )
    }  
  }
  implicit val featuresWrites = Json.writes[Features]
  implicit val shipClassWrites = new Writes[ShipClass] {
    def writes(shipClass: ShipClass) = {
      Json.obj(
        "name" -> shipClass.name,
        "priority" -> shipClass.priority
      )
    }
  }
  implicit val shipWrites = new Writes[Ship] {
    def writes(ship: Ship) = {
      Json.obj(
        "id" -> ship.id,
        "name" -> ship.name,
        "faction" -> ship.faction,
        "shipClass" -> ship.shipClass,
        "strength" -> ship.strength,
        "defense" -> ship.defense,
        "primary" -> ship.primary,
        "secondary" -> ship.secondary,
        "tertiary" -> ship.tertiary,
        "features" -> ship.features,
        "priAtkValue" -> ship.priAtkValue,
        "secAtkValue" -> ship.secAtkValue,
        "terAtkValue" -> ship.terAtkValue,
        "attackValue" -> ship.attackValue,
        "defenseValue" -> ship.defenseValue
      )
    }
  }

  def ships() = Action {
    val ships = GameData.Ships
      .filter(s => s.faction != "All")
      .sortBy(_.strength)
    Ok(Json.toJson(ships))
  }

  implicit val shipDamageWithAbsorbWrites = Json.writes[ShipDamageWithAbsorb]
  implicit val shipDamageWrites = Json.writes[ShipDamage]

  def shipsDamage() = Action {
    val ships = GameData.ShipsDamageWithAbsorb
      .sortBy(_.strength)
    Ok(Json.toJson(ships))
  }

}
