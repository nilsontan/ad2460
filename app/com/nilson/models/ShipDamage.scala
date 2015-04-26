package com.nilson.models

/**
 * Ship Damage calculated after absorb
 * @author Nilson
 * @since 4/6/2015
 */
object ShipDamage {
  def by(ship: Ship): ShipDamage = {
    ShipDamage(
      ship.id,
      ship.name,
      ship.faction,
      ship.shipClass,
      ship.strength,
      damageWithAbsorb(ship, 110),
      damageWithAbsorb(ship, 160),
      damageWithAbsorb(ship, 210),
      damageWithAbsorb(ship, 360),
      damageWithAbsorb(ship, 310),
      damageWithAbsorb(ship, 1010)
    )
  }

  private def damageWithAbsorb(ship: Ship, absorb: Int): ShipDamageWithAbsorb = {
    val (primaryDamage, secondaryDamage) = takeDamage(ship, absorb)
    val primary = ship.primary.copy(incDamage = 1).copy(damage = primaryDamage)
    val secondary = ship.secondary.copy(incDamage = 1).copy(damage = secondaryDamage)
    val newShip = ship.copy(primary = primary).copy(secondary = secondary)
    ShipDamageWithAbsorb(primaryDamage, secondaryDamage,
      primary.averageDamage, secondary.averageDamage, newShip.attackValue)
  }

  private def takeDamage(ship: Ship, absorb: Int) = {
    val p = if (ship.primary.damage == 0) 0
            else if (Math.abs(ship.primary.finalDamage - absorb) < absorb) ship.primary.finalDamage/2
            else ship.primary.finalDamage - absorb
    val s = if (ship.secondary.damage == 0) 0
            else if (Math.abs(ship.secondary.finalDamage - absorb) < absorb) ship.secondary.finalDamage/2
            else ship.secondary.finalDamage - absorb
    (p.toInt, s.toInt)
  }
}

case class ShipDamage(
                     id: Int,
                     name: String,
                     faction: String,
                     shipClass: ShipClass,
                     strength: Int,
                     frigateT1: ShipDamageWithAbsorb,
                     frigateT2: ShipDamageWithAbsorb,
                     cruiserT1: ShipDamageWithAbsorb,
                     cruiserT2: ShipDamageWithAbsorb,
                     carrier: ShipDamageWithAbsorb,
                     battleship: ShipDamageWithAbsorb
                       )

case class ShipDamageWithAbsorb(
                           primaryDamage: Int,
                           secondaryDamage: Int,
                           primaryTotalDamage: Double,
                           secondaryTotalDamage: Double,
                           attackValue: Double
                             )
