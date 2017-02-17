package me.chuwy.toodo

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext
import scala.util.Success

import io.circe._, generic.auto._, parser._, io.circe.syntax._


import Data.Item


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

  def main(): Unit = {
    val item = Item("Write this app", "sooner or later!")
    val mainElement = dom.document.getElementById("main")
    showItems(mainElement)
    println(s"A lot of things need to be done. This is one of them $item")
  }
}
