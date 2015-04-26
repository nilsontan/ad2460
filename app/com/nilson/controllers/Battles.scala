package com.nilson.controllers

import com.nilson.models.{Commander, TacticalOption}
import com.nilson.process.{GameData, BattleReporter}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

/**
 *
 * @author Nilson
 * @since 1/28/2015
 */
object Battles extends Controller {

  case class SimpleReport(id: String, name: String, atk: String, dfd: String)
  implicit val commanderFormat = Json.format[Commander]
  implicit val simpleReportFormat = Json.format[SimpleReport]

  def all() = Action {
    val reports = GameData.Reports.map{ x =>
      val r = x._2
      val id = (r \ "id").as[String]
      val name = (r \ "name").as[String]
      val atk = (r \ "attacker").as[String]
      val dfd = (r \ "defender").as[String]
//      val commanders = (r \ "commanders").as[List[Commander]]
      SimpleReport(id, name, atk, dfd)
    }
    Ok(Json.toJson(reports))
  }

  case class CombatReportHtml(title: String,
                    battleId: String,
                    tactics: String,
                    attackerName: String,
                    defenderName: String,
                    shipDetails: String,
                    combatDetails: String)

  implicit val sampleReads: Reads[CombatReportHtml] = Json.reads[CombatReportHtml]
  implicit val sampleWrites: Writes[CombatReportHtml] = Json.writes[CombatReportHtml]

  def entry() = Action(BodyParsers.parse.json) { request =>
    val result = request.body.validate[CombatReportHtml]
    result.fold(
      errors => {
        Logger.error(s"Errors while parsing entry: $errors")
        BadRequest("Wrong data given!!")
      },
      combatReportHtml => {
        val jsonReport = Json.toJson(combatReportHtml)
        val reportId = BattleReporter.insertBattle(jsonReport)
        Ok(Json.toJson(reportId))
      }
    )
  }

  def delete() = Action(BodyParsers.parse.json) { request =>
    val reportIdToBeDeleted = request.body.\("reportId").asOpt[String].getOrElse("")
    BattleReporter.deleteBattle(reportIdToBeDeleted)
    Ok(Json.toJson("Ok"))
  }

  def report(reportId: String) = Action {
    Ok(BattleReporter.getBattle(reportId))
  }

  def tacticalSettings() = Action {
    Ok(TacticalOption.settingsJson)
  }

}


