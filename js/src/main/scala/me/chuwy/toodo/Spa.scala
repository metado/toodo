package me.chuwy.toodo

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, KeyCode}
import org.scalajs.dom.raw.HTMLInputElement

import io.circe._, time._, generic.auto._, parser._, io.circe.syntax._

import cats.syntax.either._

import mhtml._

import Data.{ Item, IdItem, WithId }

object Model {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  val allItems: Var[List[Either[String, IdItem]]] = Var(Nil)

  def newItem(title: String): Unit = {
    val item = Item(title)
    createItem(item).onComplete {
      case Success(_) => allItems.update(todos => Right(WithId(1, item)) +: todos)
      case Failure(fail) => dom.console.log(fail.asInstanceOf[js.Any])
    }
  }

  def createItem(item: Item): Future[dom.XMLHttpRequest] = {
    val jsonPayload = item.asJson.noSpaces
    val headers = Map("Content-Type" -> "application/json")
    Ajax.post("http://127.0.0.1:8080/api/item", jsonPayload, headers = headers)
  }
}

object Spa extends js.JSApp {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  private def conditionalAttribute(cond: Boolean) = if (cond) "true" else null

  def getItems: Future[Either[Error, List[IdItem]]] =
    Ajax.get("http://127.0.0.1:8080/api/").map { xhr =>
      parse(xhr.responseText).flatMap { json => json.as[List[IdItem]] }
    }

  getItems.onComplete {
    case Success(Right(list)) =>
      Model.allItems := list.map(_.asRight[String])
    case Success(Left(err)) =>
      Model.allItems := List(err.toString.asLeft[IdItem])
    case Failure(err) =>
      Model.allItems := List(err.toString.asLeft[IdItem])
  }

  def updateItem(id: Long, f: IdItem => IdItem): Unit = {
    val newItems = Model.allItems.value.map {
      case Right(item) if item.id == id => Right(f(item))
      case other => other
    }
    Model.allItems := newItems
  }

  def itemNode(item: Either[String, IdItem]): xml.Node = {
    val onChange: (dom.MouseEvent) => Unit = { event: dom.MouseEvent =>
      item match {
        case Right(i) =>
          println("Changing " + i.toString)
          updateItem(i.id, it => it.copy(model = it.model.copy(title = it.model.title + " DONE", done = !it.model.done)))
        case _ => ()
      }
    }

    item match {
      case Right(item) =>
        <div>
          <span> {item.model.title} </span>
          <span> created at </span>
          <span> {item.model.createDate.toString} </span>
          <span> <input type="checkbox" checked={conditionalAttribute(item.model.done)} onchange={onChange}></input> </span>
        </div>
      case Left(err) =>
        <div>{err}</div>
    }
  }

  val newItemNode: xml.Node = {
    val onInputKeydown: (dom.KeyboardEvent) => Unit = { event: dom.KeyboardEvent =>
      (event.currentTarget, event.keyCode) match {
        case (input: HTMLInputElement, KeyCode.Enter) =>
          input.value.trim match {
            case "" =>
            case title =>
              Model.newItem(title)
              input.value = ""
          }
        case _ => ()
      }
    }
    <header class="header">
      <input class="new-todo"
             autofocus="true"
             placeholder="What needs to be done?"
             onkeydown={onInputKeydown}/>
    </header>
  }

  def app: xml.Node = {
    <div>
      <div>{ newItemNode }</div>
      <div>
        { Model.allItems.map { items => items.map(itemNode) } }
      </div>
    </div>
  }

  def main(): Unit = {
    val mainElement = dom.document.getElementById("main")
    mount(mainElement, app)
  }
}
