package me.chuwy.toodo

import scala.concurrent.{ Future, ExecutionContext }

import org.scalajs.dom

import org.scalajs.dom.ext.Ajax
import scala.scalajs.concurrent.JSExecutionContext

import io.circe._, time._, generic.auto._, io.circe.syntax._

import Data.Item

/**
  * Mostly HTTP requests, all in `Future`
  */
object Controller {

  implicit val context: ExecutionContext = JSExecutionContext.queue

  def getItems: Future[dom.XMLHttpRequest] =
    Ajax.get(Item.endpoint)

  def createItem(item: Item): Future[dom.XMLHttpRequest] = {
    val jsonPayload = item.asJson.noSpaces
    val headers = Map("Content-Type" -> "application/json")
    Ajax.post(Item.endpoint, jsonPayload, headers = headers)
  }

  def updateItem(item: Item.Stored): Future[dom.XMLHttpRequest] = {
    val jsonPayload = item.asJson.noSpaces
    val headers = Map("Content-Type" -> "application/json")
    Ajax.put(s"${Item.endpoint}/${item.id}", jsonPayload, headers = headers)
  }
}

