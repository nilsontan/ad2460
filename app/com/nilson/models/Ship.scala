package com.nilson.models

import scala.math.BigDecimal.RoundingMode

/**
 *
 * @author Nilson
 * @since 1/30/2015
 */
object Ship {
  def parseShip(id: Int,
                name: String,
                factionName: String,
                shipClassName: String,
                strengthName: String,
                defenseLine: String,
                primaryLine: String,
                secondaryLine: String,
                tertiaryLine: String,
                featuresLine: String): Ship = {
    val faction = factionName match {
      case "In" => InTech
      case "Sr" => StronTech
      case "Nd" => NeoTech
      case _    => All
    }

    val shipClass = shipClassName match {
      case "Fr" => Frigate
      case "Cr" => Cruiser
      case "Ca" => Carrier
      case "Ba" => Battleship
      case "Fi" => Fighter
      case "Co" => Corvette
      case "De" => OrbitalDefense
    }

    val strength = strengthName.toInt

    def makeDef(line: String): Defense = {
      val _def = line.split(',').map(_.toInt)
      new Defense(_def(0), _def(1), _def(2), _def(3), _def(4), 1, 1)
    }

    def makeAtk(line: String): Attack = {
      val _atk = line.split(',').map(_.toInt)
      new Attack(_atk(0), _atk(1), _atk(2), _atk(3), _atk(4), _atk(5), 1)
    }

    def makeFeat(line: String): Features = {
      val _feat = line.split(',')
      new Features(_feat(0).toInt, _feat(1).toInt, _feat(2).toInt, _feat(3).toInt, _feat(4), _feat(5))
    }

    new Ship(id, name, faction.name, shipClass, strength,
      makeDef(defenseLine),
      makeAtk(primaryLine),
      makeAtk(secondaryLine),
      makeAtk(tertiaryLine),
      makeFeat(featuresLine))
  }

  def roundAt(p: Int)(n: Double): Double = { BigDecimal(n).setScale(p, BigDecimal.RoundingMode.HALF_UP).toDouble }
  def roundAt0(n: Double): Double = roundAt(0)(n)
  def roundAt2(n: Double) = roundAt(2)(n)
}

case class Ship(
            id: Int,
            name: String,
            faction: String,
            shipClass: ShipClass,
            strength: Int,
            defense: Defense,
            primary: Attack,
            secondary: Attack,
            tertiary: Attack,
            features: Features) {

  val attackValue = strength match {
    case 0 => 0
    case _ => Ship.roundAt2((primary.averageDamage + secondary.averageDamage)/strength)
  }

  val priAtkValue = strength match {
    case 0 => 0
    case _ => Ship.roundAt2(primary.averageDamage/strength)
  }

  val secAtkValue = strength match {
    case 0 => 0
    case _ => Ship.roundAt2(secondary.averageDamage/strength)
  }

  val terAtkValue = strength match {
    case 0 => 0
    case _ => Ship.roundAt2(tertiary.averageDamage/strength)
  }

  val defenseValue = strength match {
    case 0 => 0
    case _ => Ship.roundAt2((defense.hull * 1.25 + defense.shield)/strength)
  }

  def incTargeting(inc: Int): Ship = { incPriTargeting(inc).incSecTargeting(inc) }
  def incPriTargeting(inc: Int): Ship = { copy(primary = primary.incTargeting(inc)) }
  def incSecTargeting(inc: Int): Ship = { copy(secondary = secondary.incTargeting(inc)) }
  
  def incRange(inc: Int): Ship = { incPriRange(inc).incSecRange(inc) }
  def incPriRange(inc: Int): Ship = { copy(primary = primary.incRange(inc)) }
  def incSecRange(inc: Int): Ship = { copy(secondary = secondary.incRange(inc)) }
  
  def incCritChance(inc: Int): Ship = { incPriCritChance(inc).incSecCritChance(inc) }
  def incPriCritChance(inc: Int): Ship = { copy(primary = primary.incCritChance(inc)) }
  def incSecCritChance(inc: Int): Ship = { copy(secondary = secondary.incCritChance(inc)) }
  
  def incDamage(inc: Int): Ship = { incPriDamage(inc).incSecDamage(inc) }
  def incPriDamage(inc: Int): Ship = { copy(primary = primary.incDamage(inc)) }
  def incSecDamage(inc: Int): Ship = { copy(secondary = secondary.incDamage(inc)) }
  def incTerDamage(inc: Int): Ship = { copy(tertiary = tertiary.incDamage(inc)) }
  def addPriDamage(inc: Int): Ship = { copy(primary = primary.copy(damage = primary.damage + inc)) }

  def incHull(inc: Int): Ship = { copy(defense = defense.incHull(inc)) }
  def incShield(inc: Int): Ship = { copy(defense = defense.incShield(inc)) }
  def incShieldRecharge(inc: Int): Ship = { copy(defense = defense.incShieldRecharge(inc)) }
  def incAbsorb(inc: Int): Ship = { copy(defense = defense.incAbsorb(inc)) }
  def incManeuverability(inc: Int): Ship = { copy(defense = defense.incManeuverability(inc)) }

  def incCargoCapacity(inc: Int): Ship = { copy(features = features.incCargoCapacity(inc)) }
  def incHangarCapacity(inc: Int): Ship = { copy(features = features.incHangarCapacity(inc)) }
  def incJumpRange(inc: Int): Ship = { copy(features = features.incJumpRange(inc)) }
  def decVolatility(inc: Int): Ship = { copy(features = features.decVolatility(inc)) }

  override def toString: String = {
    List(name + " " + primary.damage) mkString ""
  }
}

case class Attack(
              guns: Int,
              range: Int,
              damage: Int,
              targeting: Int,
              rateOfFire: Int,
              critChance: Int,
              incDamage: Double) {

  val fDmg = damage * incDamage
  val finalDamage = Ship.roundAt0(fDmg)
  val dmg = guns * rateOfFire * fDmg
  val averageDamage = Ship.roundAt0(dmg * (100 - critChance).toDouble/100 + dmg * 2 * critChance.toDouble/100)

  def incTargeting(inc: Int): Attack = {
    if (guns > 0)
      copy(targeting = targeting + inc)
    else
      this
  }
  
  def incRange(inc: Int): Attack = {
    if (guns > 0)
      copy(range = range + inc)
    else
      this
  }
  
  def incCritChance(inc: Int): Attack = {
    if (guns > 0)
      copy(critChance = critChance + inc)
    else
      this
  }
  
  def incDamage(inc: Int): Attack = {
    copy(incDamage = incDamage + inc.toDouble/100)
  }
}

case class Defense(
               hull: Int,
               absorbs: Int,
               shield: Int,
               shieldRecharge: Int,
               maneuverability: Int,
               incHull: Double,
               incShield: Double) {

  val finalHull = Ship.roundAt0(hull * incHull)
  val finalShield = Ship.roundAt0(shield * incShield)

  def incHull(inc: Int): Defense = {
    copy(incHull = incHull + inc.toDouble/100)
  }

  def incShield(inc: Int): Defense = {
    copy(incShield = incShield + inc.toDouble/100)
  }

  def incShieldRecharge(inc: Int): Defense = {
    copy(shieldRecharge = Ship.roundAt0(shieldRecharge + inc.toDouble/100).toInt)
  }

  def incAbsorb(inc: Int): Defense = {
    copy(absorbs = absorbs + inc)
  }

  def incManeuverability(inc: Int): Defense = {
    if (maneuverability > 0)
      copy(maneuverability = maneuverability + inc)
    else
      this
  }
}

case class Features(
                cargoCapacity: Int,
                hangarCapacity: Int,
                jumpRange: Int,
                volatility: Int,
                priSpecialEffect: String,
                secSpecialEffect: String) {

  def incCargoCapacity(inc: Int): Features = {
    copy(cargoCapacity = (cargoCapacity * (1 + inc.toDouble/100)).toInt)
  }

  def incJumpRange(inc: Int): Features = {
    copy(jumpRange = (jumpRange * (1 + inc.toDouble/100)).toInt)
  }

  def incHangarCapacity(inc: Int): Features = {
    copy(hangarCapacity = hangarCapacity + inc)
  }

  def decVolatility(dec: Int): Features = {
    copy(volatility = volatility - dec)
  }
}