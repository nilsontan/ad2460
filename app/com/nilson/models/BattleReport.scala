package com.nilson.models

/**
 *
 * @author Nilson
 * @since 2/2/2015
 */
case class BattleReport(id: String,
                        name: String,
                        attacker: String,
                        defender: String,
                        attackerTactics: Tactics,
                        defenderTactics: Tactics,
                        turns: List[BattleTurn],
                        summary: List[List[CombatShip]],
                        commanders: List[Commander])
