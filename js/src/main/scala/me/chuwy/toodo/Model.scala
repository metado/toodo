package me.chuwy.toodo

import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom

import cats.syntax.either._

import io.circe._, time._, generic.auto._, parser._

import mhtml.Var

import Data.Item

/**
  * Mostly access to `Model.allItems`
  */
object Model {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  val allItems: Var[List[Either[String, Item.Stored]]] = Var(Nil)

  def newItem(title: String): Unit = {
    val item = Item(title)
    Controller.createItem(item).onComplete {
      case Success(req) =>
        val newItem = parse(req.responseText).flatMap(_.as[Item.Stored]).leftMap(_.toString)
        allItems.update(todos => newItem +: todos)
      case Failure(fail) =>
        dom.console.log(fail.asInstanceOf[js.Any])
    }
  }

  def updateItem(id: Long, f: Item.Stored => Item.Stored): Unit = {
    val newItems = Model.allItems.value.map {
      case Right(item) if item.id == id => Right(f(item))
      case other => other
    }
    Model.allItems := newItems
  }

  def parseIntoItems(xhr: dom.XMLHttpRequest): Either[String, List[Item.Stored]] = {
    parse(xhr.responseText).flatMap { json => json.as[List[Item.Stored]] }.leftMap(_.toString)
  }

}

