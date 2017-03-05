package me.chuwy.toodo

import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom

import cats.syntax.either._

import io.circe._, time._, generic.auto._, parser._

import mhtml.{ Var, Rx }

import Data.Item

/**
  * Mostly access to `Model.allItems`
  */
object Model {

  type ItemList = List[Either[String, Item.Stored]]

  implicit val context: ExecutionContext = JSExecutionContext.queue

  private val allItems: Var[ItemList] = Var(Nil)

  val getAllItems: Rx[ItemList] = allItems

  def updateItems(newItems: ItemList): Unit =
    allItems := newItems.sortBy {
      case Right(item) => (item.model.done, item.model.createDate.toString)
      case Left(err) => (false, "")
    }

  def newItem(title: String): Unit = {
    val item = Item(title)
    Controller.createItem(item).onComplete {
      case Success(req) =>
        val newItem = parse(req.responseText).flatMap(_.as[Item.Stored]).leftMap(_.toString)
        updateItems(newItem :: allItems.value)
      case Failure(fail) =>
        dom.console.log(fail.asInstanceOf[js.Any])
    }
  }

  def updateItem(newItem: Item.Stored): Unit = {
    val newItems = Model.allItems.value.map {
      case Right(item) if item.id == newItem.id => Right(newItem)
      case other => other
    }
    updateItems(newItems)
  }

  def parseIntoItems(xhr: dom.XMLHttpRequest): Either[String, List[Item.Stored]] = {
    parse(xhr.responseText).flatMap { json => json.as[List[Item.Stored]] }.leftMap(_.toString)
  }
}

