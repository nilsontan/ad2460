package com.nilson.models

/**
 *
 * @author Nilson
 * @since 2/2/2015
 */
case class CombatShip(name: String,
                      id: Int,
                      strength: Int,
                      shipClass: String,
                      shipClassValue: Int,
                      intact: Int,
                      died: Int,
                      damageHull: Int,
                      damageShields: Int,
                      damagePrimary: Int,
                      damageSecondary: Int,
                      damageTertiary: Int,
                      damageTotal: Int,
                      shotsPrimaryHit: Int,
                      shotsPrimaryMiss: Int,
                      shotsPrimaryTotal: Int,
                      shotsSecondaryHit: Int,
                      shotsSecondaryTotal: Int,
                      shotsTertiaryHit: Int,
                      shotsTertiaryTotal: Int)
