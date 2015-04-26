package com.nilson.models

/**
 *
 * @author Nilson
 * @since 1/29/2015
 */
sealed trait Faction { def name: String }
case object All extends Faction { val name = "All" }
case object InTech extends Faction { val name = "In" }
case object StronTech extends Faction { val name = "Sr" }
case object NeoTech extends Faction { val name = "Nd" }