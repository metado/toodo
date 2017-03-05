package me.chuwy.toodo

import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.HTMLInputElement

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import Data.Item

/**
  * Only xml.Node, no http requests
  */
object View {

  val formatter = DateTimeFormatter.ofPattern("E dd LLL")
  def formatCreateDate(dateTime: LocalDateTime): String =
    formatter.format(dateTime)

  implicit val context: ExecutionContext = JSExecutionContext.queue

  def app: xml.Node = {
    <div>
      <div>{ newItemNode }</div>
      <div>
        { Model.allItems.map { items => items.map(View.itemNode) } }
      </div>
    </div>
  }

  def itemNode(item: Either[String, Item.Stored]): xml.Node = {
    val onChange: (dom.MouseEvent) => Unit = { event: dom.MouseEvent =>
      item match {
        case Right(i) =>
          val newItem = Data.Item.doneLens.set(i)(!i.model.done)
          Controller.updateItem(newItem).onComplete {
            case Failure(err) =>
              dom.console.log(err.asInstanceOf[js.Any])
            case Success(_) =>
              Model.updateItem(newItem)
          }
        case _ => ()
      }
    }

    item match {
      case Right(item) =>
        val checkboxId = s"checkbox-${item.id}"
        <div class="item">
          <label for={checkboxId}>
            <span><input id={checkboxId} type="checkbox" checked={conditionalAttribute(item.model.done)} onchange={onChange}></input> </span>
            <span>{item.model.title}</span>
            <span class="item__create-date"> {formatCreateDate(item.model.createDate)} </span>
          </label>
        </div>
      case Left(err) =>
        <div class="item--error">{err}</div>
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

  private def conditionalAttribute(cond: Boolean) = if (cond) "true" else null
}

