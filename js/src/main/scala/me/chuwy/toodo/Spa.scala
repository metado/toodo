package me.chuwy.toodo

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, KeyCode}
import org.scalajs.dom.raw.HTMLInputElement

import io.circe._, generic.auto._, parser._, io.circe.syntax._

import mhtml._

import Data.Item

object Model {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  val allItems: Var[List[Item]] = Var(Nil)

  def newItem(title: String): Unit = {
    val item = Item(title, done = false, "just now")
    createItem(item).onComplete {
      case Success(_) => allItems.update(todos => item +: todos)
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

  def getItems: Future[Either[Error, List[Item]]] =
    Ajax.get("http://127.0.0.1:8080/api/").map { xhr =>
      parse(xhr.responseText).flatMap { json => json.as[List[Item]] }
    }

  getItems.onComplete {
    case Success(Right(list)) =>
      Model.allItems := list
    case _ => ()
  }

  def itemNode(item: Item): xml.Node = {
    <div>
      <span> {item.title} </span>
      <span> created at </span>
      <span> {item.createDate} </span>
      <span> <input type="checkbox" checked={conditionalAttribute(item.done)}></input> </span>
    </div>
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
