package com.nilson.models

import play.api.libs.json.Json

import scala.collection.immutable.ListMap

/**
 *
 * @author Nilson
 * @since 1/29/2015
 */
case class TacticalOption(name: String)

object TacticalOption {
  implicit val tacticalOptionFormat = Json.format[TacticalOption]

  val targetPriority = ListMap(
    "cs" -> "Closest Ship",
    "lh" -> "Least Hull",
    "mh" -> "Most Hull",
    "mf" -> "Most Firepower",
    "ls" -> "Least Speed",
    "ms" -> "Most Speed"
  )

  val targetReEvaluate = ListMap(
    "yes" -> "Yes",
    "no" -> "No"
  )

  val settingsFighter = ListMap(
    "cd" -> "Corvette Defense",
    "csd" -> "Capital Ship Defense",
    "da" -> "Defend All",
    "ss" -> "Ship to Ship"
  )

  val settingsCorvette = ListMap(
    "co" -> "Corvette Offense",
  "cd" -> "Corvette Defense"
  )

  val retreat1 = ListMap(
    "r75" -> "75%",
  "r60" -> "60%",
  "r45" -> "45%",
  "r30" -> "30%",
  "r15" -> "15%",
  "r0" -> "Never"
  )

  val retreat2 = ListMap(
    "cs" -> "Capital Ships",
    "th" -> "Total Hull",
    "tf" -> "Total Firepower"
  )

  val settings = ListMap(
    "targetPriority" -> targetPriority,
    "targetReEvaluate" -> targetReEvaluate,
    "settingsFighter" -> settingsFighter,
    "settingsCorvette" -> settingsCorvette,
    "retreat1" -> retreat1,
    "retreat2" -> retreat2)

  val settingsJson = Json.arr(settings.map { case (k, v) =>
    Json.obj(k ->
      v.map { case (k2, v2) =>
        Json.obj("key" -> k2, "name" -> v2)
      })
  })
}
