package com.nilson.process

import java.io.File

import com.nilson.models._
import play.api.Play.current
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.{Logger, Play}

/**
 *
 * @author Nilson
 * @since 1/28/2015
 */
object BattleReporter {

  val sep = System.getProperty("file.separator")
  val battleFolder = Play.configuration.getString("application.sourceFolder").get

  implicit val commanderFormat = Json.format[Commander]
  implicit val tacticsReads = Json.reads[Tactics]
  implicit val tacticsWrites = Json.writes[Tactics]
  implicit val combatShipReads = Json.reads[CombatShip]
  implicit val combatShipWrites = Json.writes[CombatShip]
  implicit val combatantReads = Json.reads[CombatantTurn]
  implicit val combatantWrites = new Writes[CombatantTurn] {
    def writes(combatant: CombatantTurn) = {
      val pri = combatant.ships.map(_.damagePrimary).sum
      val sec = combatant.ships.map(_.damageSecondary).sum
      val ter = combatant.ships.map(_.damageTertiary).sum
      val hull = combatant.ships.map(_.damageHull).sum
      val shields = combatant.ships.map(_.damageShields).sum
      val secHit = combatant.ships.map(_.shotsSecondaryHit).sum
      val secTotal = combatant.ships.map(_.shotsSecondaryTotal).sum
      val strength = combatant.ships.map(s => s.strength * (s.intact + s.died)).sum
      Json.obj(
        "ships" -> combatant.ships,
        "totalDamagePrimary" -> pri,
        "totalDamageSecondary" -> sec,
        "totalDamageTertiary" -> ter,
        "totalDamage" -> (pri + sec + ter),
        "totalDamageHull" -> hull,
        "totalDamageShields" -> shields,
        "totalSecondaryHit" -> secHit,
        "accuracy" -> (secHit + "/" + secTotal),
        "strength" -> strength,
        "skill" -> combatant.skill
      )
    }
  }
  implicit val turnReads = Json.reads[BattleTurn]
  implicit val turnWrites = Json.writes[BattleTurn]
  implicit val battleReportReads = Json.reads[BattleReport]
  implicit val battleReportWrites = Json.writes[BattleReport]

  def files = {
    new File(battleFolder).listFiles()
      .filter(_.isFile)
      .filter(_.getName.endsWith("txt"))
      .toList
  }

  def changeFileNames() = {
    files.foreach { f =>
      val fileName = f.getNameWithoutExts
      val b = processBattle(fileName)
      val newFileName = List(b.id, b.name, b.attacker, b.defender) mkString "--"
      val newFile = new File(battleFolder + sep + newFileName + ".txt")
      f.renameTo(newFile)
    }
  }

  def allReports(): Map[String, JsValue] = {
    files.map { f =>
      val fileName = f.getNameWithoutExts split "--"
//      fileName -> Json.toJson(processBattle(fileName))
      fileName(0) -> Json.obj(
        "id" -> Json.toJson(fileName(0)),
        "name" -> Json.toJson(fileName(1)),
        "attacker" -> Json.toJson(fileName(2)),
        "defender" -> Json.toJson(fileName(3))
      )
    }.toMap
  }

  def processAll() {
    files.foreach(processFile)
  }

  def getBattle(id: String): JsValue = {
    val file = files.find(_.getNameWithoutExts.startsWith(id + "--"))
    val report = processFile(file.get)
    Json.toJson(report)
  }

  def deleteBattle(id: String): Unit = {
    val file = files.find(_.getNameWithoutExts.startsWith(id + "--"))
    try {
      file.get.delete()
      GameData.removeReport(id)
    } finally { }
  }

  def insertBattle(rawReport: JsValue): String = {

    val text = Json.stringify(rawReport)
    val newFileName = List(
      jsValueToString(rawReport \ "battleId"),
      jsValueToString(rawReport \ "title"),
      jsValueToString(rawReport \ "attackerName"),
      jsValueToString(rawReport \ "defenderName")
    ) mkString "--"
    val file2 = new File(battleFolder + sep + newFileName + ".txt")
    printToFile(file2) { p =>
      p.println(text)
    }

    val battleReport = processJsBattle(rawReport)
    GameData.addReport(
      battleReport.id -> Json.obj(
        "id" -> Json.toJson(battleReport.id),
        "name" -> Json.toJson(battleReport.name),
        "attacker" -> Json.toJson(battleReport.attacker),
        "defender" -> Json.toJson(battleReport.defender)
      )
    )

    battleReport.id
  }

  def jsValueToString(jsValue: JsValue) = jsValue.asOpt[String].get

  def processJsBattle(reportJs: JsValue): BattleReport = {

    val battleReportTemp = new BattleReportTemp(jsValueToString(reportJs \ "battleId"), jsValueToString(reportJs \ "title"))
    battleReportTemp.attackerName = jsValueToString(reportJs \ "attackerName")
    battleReportTemp.defenderName = jsValueToString(reportJs \ "defenderName")
    parseTactics(jsValueToString(reportJs \ "tactics"), battleReportTemp)
    parseShips(jsValueToString(reportJs \ "shipDetails"), battleReportTemp)
    parseBattleText(jsValueToString(reportJs \ "combatDetails"), battleReportTemp)

    convertToReport(battleReportTemp)
  }

  def processBattle(battleName: String): BattleReport = {
    val file = files.find(_.getNameWithoutExts == battleName)
    processFile(file.get)
  }

  implicit class BattleReportUtility(val s: File) {
    def getNameWithoutExts = s.getName.replaceFirst("[.][^.]+$", "")
  }

  private def processFile(file: File): BattleReport = {
    val source = scala.io.Source.fromFile(file)
    val line = source.getLines().mkString
    val reportJs = Json.parse(line)
    source.close()
    processJsBattle(reportJs)
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  private def convertToReport(temp: BattleReportTemp): BattleReport = {

    val turns: List[BattleTurn] = getTurns(temp.turns.toList)
    val sumShips: List[List[CombatShip]] = getSummary(temp)
    val commanders: List[Commander] = getCommanders(temp)
    new BattleReport(temp.id, temp.name, temp.attackerName, temp.defenderName,
      getTactics(temp.attackerTactics), getTactics(temp.defenderTactics),
      turns, sumShips, commanders)
  }

  private def getCommanders(battleReport: BattleReportTemp): List[Commander] = {
    battleReport.commanders.map(c => new Commander(c._2.name, c._2.xp)).toList
  }

  private def parseTactics(line: String, battleReportTemp: BattleReportTemp) = {
    val tacticsLine = line.split("\\|")

    battleReportTemp.attackerTactics = parseTactics(tacticsLine(0))
    battleReportTemp.defenderTactics = parseTactics(tacticsLine(1))

    def parseTactics(line: String): TacticsTemp = {
      val t = line.split(',')
      val tactic = new TacticsTemp()
      tactic.targetPriority = matchTacticalOptions(0, t(0))
      tactic.targetReEvaluate = matchTacticalOptions(1, t(1))
      tactic.settingsFighter = matchTacticalOptions(2, t(2))
      tactic.settingsCorvette = matchTacticalOptions(3, t(3))
      tactic.retreat1 = matchTacticalOptions(4, t(4))
      tactic.retreat2 = matchTacticalOptions(5, t(5))
      tactic
    }

    def matchTacticalOptions(num: Int, s: String): String = TacticalOption.settings.map(_._2).toList(num)(s)
  }

  private def getSummary(temp: BattleReportTemp) = {
    val attacker = getCombatantDetails(temp.summAttk)
    val defender = getCombatantDetails(temp.summDefd)
    List(attacker.ships, defender.ships)
  }

  private def getTurns(turns: List[TurnTemp]) = {
    turns.map { turn =>
      val attacker = getCombatantDetails(turn.attacker)
      val defender = getCombatantDetails(turn.defender)
      new BattleTurn(attacker, defender)
    }
  }

  private def getTactics(tacticsTemp: TacticsTemp): Tactics = {
    new Tactics(
      tacticsTemp.targetPriority,
      tacticsTemp.targetReEvaluate,
      tacticsTemp.settingsFighter,
      tacticsTemp.settingsCorvette,
      tacticsTemp.retreat1,
      tacticsTemp.retreat2
    )
  }

  private def getCombatantDetails(combatantTemp: CombatantTemp) = {
    val ships = combatantTemp.ships.filter(x => !List(432, 433, 435, 436)
      .contains(x._2.ship.id)).map { case (name, ship) =>
//      if !List(432, 433, 435, 436).contains(ship.ship.id)
      new CombatShip(name,
        ship.ship.id,
        ship.ship.strength,
        ship.ship.shipClass.name,
        ship.ship.shipClass.priority,
        ship.intact,
        ship.exploded,
        ship.damageHull,
        ship.damageShields,
        ship.damagePrimary,
        ship.damageSecondary,
        ship.damageTertiary,
        ship.damageTotal,
        ship.shotsPrimaryHit,
        ship.shotsPrimaryMissed,
        ship.shotsPrimaryROF,
        ship.shotsSecondaryHit,
        ship.shotsSecondaryROF,
        ship.shotsTertiaryHit,
        ship.shotsTertiaryROF)
    }.toList.sortBy(_.shipClassValue)
    new CombatantTurn(ships, combatantTemp.skill)
  }

  private def parseShips(line: String, battleReport: BattleReportTemp) = {
    val shipQty = """([\w\s\-]+),([\w\s\-]+)""".r
    val info = line.split("\\|\\|\\|")

    info(0).split('|').foreach {
      case shipQty(mg@_*) => battleReport.attacker.addShip(mg.head.toInt, mg(1).toInt)
      case m => Logger.warn(s"parseShips: Unknown line : $m")
    }
    info(1).split('|').foreach {
      case shipQty(mg@_*) => battleReport.defender.addShip(mg.head.toInt, mg(1).toInt)
      case m => Logger.warn(s"parseShips: Unknown line : $m")
    }
    battleReport.firstTurn()
  }

  private def parseBattleText(lines: String, battleReport: BattleReportTemp): Unit = {
    val toRemove = "()".toSet
    val line = """(?<=<s>)(.*)""".r
    val attack = """<a>([\w\s\-]+) hits <d>([\w\s\-]+) for (\d+) points[.]* (\w+)[.]* Shields hit for (\d+)""".r
    val defend = """<d>([\w\s\-]+) hits <a>([\w\s\-]+) for (\d+) points[.]* (\w+)[.]* Shields hit for (\d+)""".r
    val attackMiss = """<a>([\w\s\-]+) misses when trying to shoot at the <d>([\w\s\-]+)""".r
    val defendMiss = """<d>([\w\s\-]+) misses when trying to shoot at the <a>([\w\s\-]+)""".r
    val attackDies = """<a>([\w\s\-]+) dies!""".r
    val defendDies = """<d>([\w\s\-]+) dies!""".r
    val endTurn = """End of turn (\d+)""".r
    val battleEnd = """The combat has been resolved""".r
    val retreat = """([\w\s\-]+) fleets retreat.""".r
    val retreat2 = """([\w\s\-]+) retreats due to: ([\w\s\-]+).""".r
    val plunder = """Fleet ([\w\s\-]+) plundered .*""".r
    val commSkill = """<([ad])>[\w\s\-]+ has decided to make use of ([\w\s\-]+)!""".r
    val commGainXp = """([\w\s\-]+) gained (\d+) XP.*""".r
    val commPromoted = """([\w\s\-]+) was promoted to.*""".r
    val commDies = """([\w\s\-]+) was killed""".r
    // ignore these
    val combatSpeed = """Combat-speed set to ([\w\s\-]+)""".r
    lines.split("<e>") foreach { x => line.findFirstIn(x.filterNot(toRemove)) foreach {
      // process each battle line here
      case attack(mg@_*) => battleReport.inputEvent(AttHit(mg))
      case attackMiss(mg@_*) => battleReport.inputEvent(AttMiss(mg))
      case attackDies(mg@_*) => battleReport.inputEvent(AttDied(mg))
      case defend(mg@_*) => battleReport.inputEvent(DefHit(mg))
      case defendMiss(mg@_*) => battleReport.inputEvent(DefMiss(mg))
      case defendDies(mg@_*) => battleReport.inputEvent(DefDied(mg))
      case commSkill(mg@_*) => battleReport.inputEvent(CommSkill(mg))
      case commGainXp(mg@_*) => battleReport.inputEvent(CommXp(mg))
      case commPromoted(mg@_*) => battleReport.inputEvent(CommPromoted(mg))
      case commDies(mg@_*) => battleReport.inputEvent(CommDied(mg))
      case endTurn(mg@_*) => battleReport.inputEvent(EndTurn(mg))
      case battleEnd(mg@_*) => battleReport.inputEvent(EndBattle(mg))
      case retreat(mg@_*) => battleReport.inputEvent(Retreat(mg))
      case retreat2(mg@_*) => battleReport.inputEvent(Retreat2(mg))
      case plunder(mg@_*) => battleReport.inputEvent(Plunder(mg))
      case combatSpeed(mg@_*) =>
      case m => Logger.warn(s"parseBattleText: Unknown line : $m")
    }
    }
  }
}

abstract class BattleEvent

case class AttHit(line: Seq[String]) extends BattleEvent

case class AttMiss(line: Seq[String]) extends BattleEvent

case class AttDied(line: Seq[String]) extends BattleEvent

case class DefHit(line: Seq[String]) extends BattleEvent

case class DefMiss(line: Seq[String]) extends BattleEvent

case class DefDied(line: Seq[String]) extends BattleEvent

case class CommXp(line: Seq[String]) extends BattleEvent

case class CommSkill(line: Seq[String]) extends BattleEvent

case class CommPromoted(line: Seq[String]) extends BattleEvent

case class CommDied(line: Seq[String]) extends BattleEvent

case class EndTurn(line: Seq[String]) extends BattleEvent

case class EndBattle(line: Seq[String]) extends BattleEvent

case class Retreat(line: Seq[String]) extends BattleEvent

case class Retreat2(line: Seq[String]) extends BattleEvent

case class Plunder(line: Seq[String]) extends BattleEvent

/*
  A battle has 2 players - attacker and defender
  Each player has a fleet - some ships, some commanders
  Each player has a tactical setting
  A battle is split into turns
  During each turn,
  - the ships fire at each other according to tactical settings
  - ships die when hull == 0
  - commander gets xp for the kill
  Repeat until the retreat condition is met
 */
class BattleReportTemp(val id: String, val name: String) {
  val attacker = new CombatantTemp("attacker")
  val defender = new CombatantTemp("defender")
  var summAttk: CombatantTemp = _
  var summDefd: CombatantTemp = _
  var attackerName: String = _
  var defenderName: String = _
  var attackerTactics: TacticsTemp = _
  var defenderTactics: TacticsTemp = _

  var commanders = Map[String, CommanderTemp]()
  val turns = scala.collection.mutable.MutableList[TurnTemp]()

  var currentTurn: TurnTemp = _
  var retreat = false

  def turn(t: Int) = {
    turns(t)
  }

  def firstTurn(): Unit = {
    currentTurn = new TurnTemp(attacker.copy(), defender.copy())
  }

  def newTurn(): Unit = {
    turns += currentTurn
    currentTurn = new TurnTemp(attacker.copy(), defender.copy())
  }

  def retreatBattle(): Unit = {
    retreat = true
  }

  def summarize(): Unit = {
    if (!retreat)
      turns += currentTurn

    summAttk = attacker.copy()
    summDefd = defender.copy()
    summAttk.ships.foreach(_._2.intact = 0)
    summDefd.ships.foreach(_._2.intact = 0)
    turns.foreach { t =>
      summAttk = addShipStats(t.attacker, summAttk)
      summDefd = addShipStats(t.defender, summDefd)
    }

    def addShipStats(combatantTemp: CombatantTemp, summ: CombatantTemp) = {
      combatantTemp.ships.foreach { s =>
        if (s._2.damageTotal > 0) {
          summ.ship(s._1).damageHull += s._2.damageHull
          summ.ship(s._1).damageShields += s._2.damageShields
          summ.ship(s._1).damagePrimary += s._2.damagePrimary
          summ.ship(s._1).damageSecondary += s._2.damageSecondary
          summ.ship(s._1).damageTertiary += s._2.damageTertiary
          summ.ship(s._1).shotsPrimaryHit += s._2.shotsPrimaryHit
          summ.ship(s._1).shotsPrimaryMissed += s._2.shotsPrimaryMissed
          summ.ship(s._1).shotsSecondaryHit += s._2.shotsSecondaryHit
          summ.ship(s._1).intact += s._2.intact + s._2.exploded
        }
        summ.ship(s._1).exploded += s._2.exploded
      }
      summ
    }

//    for (t <- turns) {
//      for (s <- t.attacker.ships) {
//                if (s._2.damageTotal > 0) {
////                summAttk.ship(s._1).damageDealt(s._2.damageTotal, 0, "pri")
//                  summAttk.ship(s._1).damageHull += s._2.damageHull
//                  summAttk.ship(s._1).damageShields += s._2.damageShields
//                  summAttk.ship(s._1).damagePrimary += s._2.damagePrimary
//                  summAttk.ship(s._1).damageSecondary += s._2.damageSecondary
//                  summAttk.ship(s._1).shotsPrimaryHit += s._2.shotsPrimaryHit
//                  summAttk.ship(s._1).shotsSecondaryHit += s._2.shotsSecondaryHit
//                  summAttk.ship(s._1).intact += s._2.intact + s._2.exploded
//                }
//        summAttk.ship(s._1).exploded += s._2.exploded
//      }
//      for (s <- t.defender.ships) {
//                if (s._2.damageTotal > 0) {
//        summDefd.ship(s._1).damageDealt(s._2.damageTotal, 0, "pri")
//        summDefd.ship(s._1).intact += s._2.intact + s._2.exploded
//                }
//        summDefd.ship(s._1).exploded += s._2.exploded
//      }
//    }
  }

  def gainXp(comm: String, xp: Int) = {
    commanders.get(comm) match {
      case Some(c) => c.xp += xp
      case None => commanders += (comm -> new CommanderTemp(comm, xp))
    }
  }

  def died(comm: String) = {
    commanders = commanders - comm
  }

  def inputEvent(battleEvent: BattleEvent) {
    battleEvent match {
      case AttHit(line) => currentTurn.attacker.ship(line.head).damageDealt(line(2).toInt, line(4).toInt, line(3))
      case AttMiss(line) => currentTurn.attacker.ship(line.head).miss()
      case AttDied(line) => currentTurn.attacker.ship(line.head).died()
                            attacker.ship(line.head).intact -= 1
      case DefHit(line) => currentTurn.defender.ship(line.head).damageDealt(line(2).toInt, line(4).toInt, line(3))
      case DefMiss(line) => currentTurn.defender.ship(line.head).miss()
      case DefDied(line) => currentTurn.defender.ship(line.head).died()
                            defender.ship(line.head).intact -= 1
      case CommXp(line) => gainXp(line.head, line(1).toInt)
      case CommSkill(line) => line.head match {
                                case "a"  => currentTurn.attacker.useSkill(line(1))
                                case "d"  => currentTurn.defender.useSkill(line(1))
                                case m    => Logger.warn(s"CommSkill: Invalid entry : $m")
                              }
      case CommPromoted(line) =>
      case CommDied(line) => died(line.head)
      case EndTurn(line) => newTurn()
      case EndBattle(line) => summarize()
      case Retreat(line) => retreatBattle()
      case Retreat2(line) =>
      case Plunder(line) =>
      case m => Logger.info(s"inputEvent: Unused line : $m")
    }
  }
}

class CommanderTemp(val name: String, var xp: Int)

class TacticsTemp() {
  var targetPriority: String = _
  var targetReEvaluate: String = _
  var settingsFighter: String = _
  var settingsCorvette: String = _
  var retreat1: String = _
  var retreat2: String = _
}

case class TurnTemp(attacker: CombatantTemp, defender: CombatantTemp)

class CombatantTemp(name: String) {

  var tactics: TacticsTemp = _
  var ships: Map[String, ShipInCombatTemp] = Map()
  var skill = ""

  def useSkill(skill: String) = {
    this.skill = skill
  }

  def ship(shipName: String) = {
    ships(shipName)
  }

  def addShip(shipId: Int, qty: Int) = {
    val ship = GameData.ShipsMap.find(p => p._2.id == shipId).getOrElse(
      throw new IllegalArgumentException(s"No such ship: $shipId"))
    ships += (ship._1 -> new ShipInCombatTemp(ship._2, qty))
  }

  def copy(): CombatantTemp = {
    val combatant = new CombatantTemp(name)
    combatant.tactics = tactics
    ships.foreach { case (shipName, shipInCombat) =>
      combatant.ships += (shipName -> new ShipInCombatTemp(shipInCombat.ship, shipInCombat.intact))
    }
    combatant
  }
}

class ShipInCombatTemp(val ship: Ship, var intact: Int) {
  var damageHull = 0
  var damageShields = 0
  var damagePrimary = 0
  var damageSecondary = 0
  var damageTertiary = 0
  var exploded = 0

  def damageTotal = damageHull + damageShields

  def damageAverage = intact match {
    case 0 => 0
    case q => damageTotal / q
  }

  def died() = {
    intact -= 1
    exploded += 1
  }

  def damageDealt(hull: Int, shields: Int, weaponType: String) = {
    damageHull += hull
    damageShields += shields

    weaponType match {
      case "pri" => damagePrimary += hull + shields
        shotsPrimaryHit += 1
      case "sec" => damageSecondary += hull + shields
        shotsSecondaryHit += 1
      case "ter" => damageTertiary += hull + shields
        shotsTertiaryHit += 1
      case m => Logger.info(s"weaponType: Unknown line : $m")
    }
  }

  def miss(): Unit = {
    shotsPrimaryMissed += 1
  }

  var shotsPrimaryHit = 0
  var shotsPrimaryMissed = 0
  val shotsPrimaryROF = ship.primary.rateOfFire * ship.primary.guns * intact
  var shotsSecondaryHit = 0
  var shotsSecondaryMissed = 0
  val shotsSecondaryROF = ship.secondary.rateOfFire * ship.secondary.guns * intact
  var shotsTertiaryHit = 0
  var shotsTertiaryMissed = 0
  val shotsTertiaryROF = ship.tertiary.rateOfFire * ship.tertiary.guns * intact

  override def toString: String = {
    List(intact, damageTotal, damageAverage, shotsPrimaryHit + "/" + shotsPrimaryROF, shotsSecondaryHit + "/" + shotsSecondaryROF) mkString ", "
  }
}
