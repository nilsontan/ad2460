package com.nilson.models

/**
 * A turn in a battle involves ships from both sides shooting at one another.
 *
 * @author Nilson
 * @since 2/2/2015
 */
case class BattleTurn(attacker: CombatantTurn, defender: CombatantTurn)
