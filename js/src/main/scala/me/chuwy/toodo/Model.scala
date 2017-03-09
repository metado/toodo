package me.chuwy.toodo

import scala.concurrent.ExecutionContext

import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom

import cats.syntax.either._

import io.circe._, parser._

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

  val getState: Rx[State] = state

  def parseXhr[A: Decoder](xhr: dom.XMLHttpRequest): Either[String, A] = {
    parse(xhr.responseText).flatMap(_.as[A]).leftMap(_.toString)
  }

  sealed trait ModelUpdate
  case class AddItem(item: Either[String, Item.Stored]) extends ModelUpdate
  case class InitItems(items: ItemList) extends ModelUpdate
  case class UpdateItems(items: ItemList) extends ModelUpdate
  case class SelectItem(item: Item.Stored) extends ModelUpdate
  case class UpdateItem(item: Item.Stored) extends ModelUpdate

  def foldState(state: State, action: ModelUpdate): State = action match {
    case AddItem(item) => state.copy(allItems = item :: state.allItems)
    case SelectItem(item) => state.openItem match {
      case Some(i) if i == item => state.copy(openItem = None)
      case _ => state.copy(openItem = Some(item))
    }
    case InitItems(items) => state.copy(allItems = items)
    case UpdateItem(newItem) => state.copy(allItems = state.allItems.map {
      case Right(item) if item.id == newItem.id => Right(newItem)
      case other => other
    })
    case UpdateItems(newItems) => state.copy(allItems = newItems.sortBy(sortItems))
  }

  def accept(action: ModelUpdate): Unit =
    state.update(state => foldState(state, action))

  def sortItems(i: Either[String, Item.Stored]) = i match {
    case Right(item) => (item.model.done, item.model.createDate.toString)
    case Left(err) => (false, "")
  }
}

