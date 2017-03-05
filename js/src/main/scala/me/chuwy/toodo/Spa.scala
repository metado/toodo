package me.chuwy.toodo

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom

import cats.syntax.either._

import mhtml.mount

import Data.Item

object Spa extends js.JSApp {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  Controller.getItems.map(Model.parseIntoItems).onComplete {
    case Success(Right(list)) =>
      Model.allItems := list.map(_.asRight[String])
    case Success(Left(err)) =>
      Model.allItems := List(err.toString.asLeft[Item.Stored])
    case Failure(err) =>
      Model.allItems := List(err.toString.asLeft[Item.Stored])
  }

  def main(): Unit = {
    val mainElement = dom.document.getElementById("main")
    mount(mainElement, View.app)
  }

}
