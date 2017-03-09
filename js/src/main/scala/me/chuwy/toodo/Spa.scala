package me.chuwy.toodo

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom

import cats.syntax.either._

import io.circe._, time._, generic.auto._

import mhtml.mount

import Data.Item
import Model.accept

object Spa extends js.JSApp {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  Controller.getItems.map(Model.parseXhr[List[Item.Stored]]).onComplete {
    case Success(Right(list)) =>
      accept(Model.UpdateItems(list.map(_.asRight[String])))
    case Success(Left(err)) =>
      accept(Model.UpdateItems(List(err.toString.asLeft[Item.Stored])))
    case Failure(err) =>
      accept(Model.UpdateItems(List(err.toString.asLeft[Item.Stored])))
  }

  @JSExport
  def getState = Model.getState.impure.value

  def main(): Unit = {
    val mainElement = dom.document.getElementById("main")
    mount(mainElement, View.app)
  }
}
