package com.nilson.models

/**
 *
 * @author Nilson
 * @since 1/29/2015
 */
sealed trait ShipClass { def name: String; def priority: Int }
case object Frigate extends ShipClass { val name = "Frigate"; val priority = 5 }
case object Cruiser extends ShipClass { val name = "Cruiser"; val priority = 3  }
case object Carrier extends ShipClass { val name = "Carrier"; val priority = 4  }
case object Battleship extends ShipClass { val name = "Battleship"; val priority = 2  }
case object Fighter extends ShipClass { val name = "Fighter"; val priority = 7  }
case object Corvette extends ShipClass { val name = "Corvette"; val priority = 6  }
case object OrbitalDefense extends ShipClass { val name = "OrbitalDefense"; val priority = 1  }