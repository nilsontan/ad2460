package com.nilson.process

import com.nilson.models.{ShipDamage, Ship}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.JsValue

import scala.collection.immutable.TreeMap

/**
 *
 * @author Nilson
 * @since 1/29/2015
 */
object GameData {

  val Ships = parseShipsData()
  val ShipsDamageWithAbsorb = calculateShipAbsorb(Ships)
  val ShipsMap = Ships.map(s => (s.name, s)).toMap
  var Reports = getReports

  def addReport(id: (String, JsValue)) = {
    Reports += id
  }
  
  def removeReport(id: String) = {
    Reports -= id
  }

  def getReports: Map[String, JsValue] = {
    TreeMap(BattleReporter.allReports().toSeq:_*)(implicitly[Ordering[String]].reverse)
//    BattleReporter.changeFileNames()
//    Map.empty[String, JsValue]
  }

  def parseShipsData(): List[Ship] = {
    val res = Play.application.classloader.getResource("ships.txt")
    val source = scala.io.Source.fromFile(res.getFile)
    val ships = { for {
      line <- source.getLines()
      info = line.split('|')
      ship = Ship.parseShip(
        info(0).toInt,
        info(1),
        info(2),
        info(3),
        info(4),
        info(5),
        info(6),
        info(7),
        info(8),
        info(9)
      )

    } yield ShipUpgrade.applyMaxUpgrades(ship) }.toList
    source.close()
    ships
  }

  def calculateShipAbsorb(ships: List[Ship]): List[ShipDamage] = {
    ships.filter(s => s.faction != "All")
      .filter(s => s.shipClass.name != "Fighter")
      .map {
      s => ShipDamage.by(s)
    }
  }
}