package me.chuwy.toodo

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import Data.Item

import scala.concurrent.ExecutionContext
import scala.util.Success

object Spa extends js.JSApp {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  def showItems(target: dom.Node) = {

    Ajax.get("http://127.0.0.1:8080/api/").onComplete {
      case Success(xhr) =>
        println(xhr)
        target.textContent = xhr.responseText
      case _ => println("Failure")
    }
  }

  def main(): Unit = {
    val item = Item("Write this app", "sooner or later!")
    val mainElement = dom.document.getElementById("main")
    showItems(mainElement)
    println(s"A lot of things need to be done. This is one of them $item")
  }
}
