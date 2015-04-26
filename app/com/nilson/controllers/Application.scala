package com.nilson.controllers

import play.api.Logger
import play.api.mvc.{Controller, Action}

import scala.concurrent.Future

object Application extends Controller {

  def index(any: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }

  def view(template: String) =  Action.async { implicit request =>
    template match {
      case "home" => Future.successful(Ok(views.html.home()))
      case "navigation" => Future.successful(Ok(views.html.navigation()))
      case "report" =>  Future.successful(Ok(views.html.report()))
      case "newReport" =>  Future.successful(Ok(views.html.newReport()))
      case "ships" => Future.successful(Ok(views.html.ships()))
      case "shipsDamage" => Future.successful(Ok(views.html.shipsDamage()))
      case _ => Future.successful(NotFound)
    }
  }
}