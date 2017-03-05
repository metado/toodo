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

  case class State(allItems: ItemList, openItem: Option[Item.Stored])

  private val state: Var[State] =
    Var(State(Nil, None))

  def recalculate(): Unit =
    state.update(s => s)

  val getState: Rx[State] = state

  val getAllItems: Rx[ItemList] =
    state.map(_.allItems)

  val getOpenItem: Rx[Option[Item.Stored]] =
    state.map(_.openItem)

  def updateItems(newItems: ItemList): Unit = {
    def f(i: Either[String, Item.Stored]) = i match {
      case Right(item) => (item.model.done, item.model.createDate.toString)
      case Left(err) => (false, "")
    }
    state := state.value.copy(allItems = newItems.sortBy(f))
  }

  def closeItem(): Unit =
    state := state.value.copy(openItem = None)

  def chooseItem(item: Item.Stored): Unit = {
    state := state.value.copy(openItem = Some(item))
  }

  def newItem(title: String): Unit = {
    val item = Item(title)
    Controller.createItem(item).onComplete {
      case Success(req) =>
        val newItem = parse(req.responseText).flatMap(_.as[Item.Stored]).leftMap(_.toString)
        updateItems(newItem :: getAllItems.value)
      case Failure(fail) =>
        dom.console.log(fail.asInstanceOf[js.Any])
    }
  }

  def updateItem(newItem: Item.Stored): Unit = {
    val newItems = Model.getAllItems.value.map {
      case Right(item) if item.id == newItem.id => Right(newItem)
      case other => other
    }
    updateItems(newItems)
  }


  def parseIntoItems(xhr: dom.XMLHttpRequest): Either[String, List[Item.Stored]] = {
    parse(xhr.responseText).flatMap { json => json.as[List[Item.Stored]] }.leftMap(_.toString)
  }
}

