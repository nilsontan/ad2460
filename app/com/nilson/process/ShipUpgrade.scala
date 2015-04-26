package com.nilson.process

import com.nilson.models._

/**
 *
 * @author Nilson
 * @since 4/1/2015
 */
object ShipUpgrade {

  def applyMaxUpgrades(ship: Ship): Ship = {
    val s = applyGeneralUpgrades(ship)
    s.faction match {
      case "In" => upgradeIn(s)
      case "Sr" => upgradeSr(s)
      case "Nd" => upgradeNd(s)
      case "All" => s
    }
  }

  private def upgradeIn(ship: Ship): Ship = {
    val s = ship.incHull(2)
      .incShield(2)
      .incDamage(2)
      .incSecTargeting(2)
      .incHull(5)
      .decVolatility(2)
      .incCritChance(1)
      .incHull(10)
      .incPriDamage(4)
      .incDamage(5)

    s.shipClass match {
      case Frigate | Cruiser => s.incDamage(4)
        .incShield(3)
        .incShieldRecharge(6)
        .incHull(5)
        .incHull(10)
        .incPriDamage(5)
        .incShield(5)
        .incManeuverability(5)
      case Battleship => s.incDamage(4)
        .incShield(3)
        .incShieldRecharge(6)
        .incHull(5)
        .incHull(10)
        .incPriDamage(5)
        .incPriRange(5)
        .incShield(5)
        .incManeuverability(5)
      case Carrier => s.incHangarCapacity(2)
        .incDamage(4)
        .incShield(3)
        .incShieldRecharge(6)
        .incHangarCapacity(2)
        .incHull(5)
        .incHull(10)
        .incPriDamage(5)
        .incHangarCapacity(5)
        .incShield(5)
        .incManeuverability(5)
      case Corvette => s.incSecCritChance(2)
        .incDamage(4)
        .incHull(10)
        .incHull(25)
        .incDamage(25)
        .incShield(25)
        .incManeuverability(25)
      case Fighter => s.incDamage(4)
        .incHull(10)
        .incHull(25)
        .incDamage(25)
        .incShield(25)
        .incManeuverability(25)
      case OrbitalDefense => s.incHull(10)
      case _ => s
    }
  }

  private def upgradeSr(ship: Ship): Ship = {
    val s = ship.incDamage(2)
      .incShield(2)
      .incShieldRecharge(4)
      .incPriTargeting(2)
      .incShield(4)
      .decVolatility(4)
      .incShieldRecharge(10)
      .incDamage(5)

    s.shipClass match {
      case Frigate => s.incRange(5)
        .incCritChance(2)
        .incHull(5)
        .incShield(5)
        .incShield(10)
        .incPriDamage(5)
        .incPriRange(5)
        .incManeuverability(5)
        .incTerDamage(10)
      case Cruiser | Battleship | Carrier => s.incRange(5)
        .incCritChance(2)
        .incHull(5)
        .incShield(5)
        .incShield(10)
        .incPriDamage(5)
        .incManeuverability(5)
        .incTerDamage(10)
      case Corvette => s.incSecCritChance(1)
        .addPriDamage(7)
        .incHull(25)
        .incDamage(25)
        .incShield(25)
        .incManeuverability(25)
      case Fighter => s.incSecCritChance(1)
        .addPriDamage(7)
        .incHull(25)
        .incDamage(25)
        .incShield(25)
        .incManeuverability(25)
      case OrbitalDefense => s.incDamage(3)
        .incCritChance(2)
      case _ => s
    }
  }

  private def upgradeNd(ship: Ship): Ship = {
    val s = ship.incShield(2)
      .incDamage(2)
      .incManeuverability(3)
      .incTargeting(2)
      .incDamage(4)
      .decVolatility(2)
      .incDamage(5)

    s.shipClass match {
      case Frigate | Battleship | Carrier => s.incRange(3)
        .incShieldRecharge(15)
        .incManeuverability(3)
        .incPriDamage(4)
        .incSecCritChance(2)
        .incHull(5)
        .incShield(5)
        .incManeuverability(5)
        .incManeuverability(5)
        .incPriDamage(5)
      case Cruiser => s.incRange(3)
        .incShieldRecharge(15)
        .incManeuverability(3)
        .incPriDamage(4)
        .incSecCritChance(2)
        .incHull(5)
        .incShield(5)
        .incManeuverability(5)
        .incManeuverability(5)
        .incPriDamage(5)
        .incPriRange(5)
      case Corvette => s.incSecCritChance(1)
        .incManeuverability(5)
        .incSecCritChance(2)
        .incHull(25)
        .incDamage(25)
        .incShield(25)
        .incManeuverability(25)
      case Fighter => s.incSecCritChance(1)
        .incManeuverability(5)
        .incSecCritChance(2)
        .incHull(25)
        .incDamage(25)
        .incShield(25)
        .incManeuverability(25)
      case OrbitalDefense => s.incDamage(5)
        .incRange(3)
      case _ => s
    }
  }

  private def applyGeneralUpgrades(ship: Ship): Ship = {
    val s = ship.incTargeting(2)
      .incHull(2)
      .incShield(2)
      .incRange(2)
      .incManeuverability(3)
      .incCargoCapacity(14)
      .incJumpRange(10)

    s.shipClass match {
      case Frigate | Cruiser | Battleship => s.incSecDamage(5).incAbsorb(10)
      case Carrier => s.incSecDamage(5).incAbsorb(10).incHangarCapacity(2)
      case _ => s
    }
  }
}


