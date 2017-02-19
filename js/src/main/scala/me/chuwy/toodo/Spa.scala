package me.chuwy.toodo

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, KeyCode}

import scala.concurrent.ExecutionContext
import scala.util.Success
import io.circe._
import generic.auto._
import parser._
import io.circe.syntax._
import mhtml._
import Data.Item
import org.scalajs.dom.raw.HTMLInputElement

object Model {
  val items: Var[List[Item]] = Var(Nil)
}

object Spa extends js.JSApp {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  def showItems(target: dom.Node) = {

    Ajax.get("http://127.0.0.1:8080/api/").onComplete {
      case Success(xhr) =>
        println(xhr)
        val result = parse(xhr.responseText).flatMap { json => json.as[List[Item]] }
        target.textContent = result.toString
      case _ => println("Failure")
    }
  }

  def newTodo(item: String): Unit = { println(item) }

  val header: xml.Node = {
    val onInputKeydown: (dom.KeyboardEvent) => Unit = { event: dom.KeyboardEvent =>
      (event.currentTarget, event.keyCode) match {
        case (input: HTMLInputElement, KeyCode.Enter) =>
          newTodo(input.value)
          input.value = ""
        case _ => ()

      }
    }
    <input class="new-todo" onkeydown={onInputKeydown}></input>

  }

  def app: xml.Node = {
    <div>
      {header}
    </div>
  }

  def main(): Unit = {
    val item = Item("Write this app", "sooner or later!")
    val mainElement = dom.document.getElementById("main")
    showItems(mainElement)
    println(s"A lot of things need to be done. This is one of them $item")
  }
}
