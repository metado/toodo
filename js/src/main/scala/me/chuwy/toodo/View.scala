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
        { Model.getState.map(_.allItems).map { items => items.map(View.itemNode) } }
      </div>
      { Model.getOpenItem.map { openItem => View.openItemNode(openItem) } }
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
            case Success(s) =>
              Model.updateItem(newItem)
          }
        case _ => ()
      }
    }

    def openItem(item: Item.Stored)(e: dom.MouseEvent): Unit = {
      Model.getOpenItem.value match {
        case Some(i) if i == item => Model.closeItem()
        case _ => Model.chooseItem(item)
      }
      Model.recalculate()   // I don't want this to be required
    }

    def getClasses(item: Item.Stored): List[String] = {
      val active = Model.getOpenItem.value.map(o => o.id == item.id).getOrElse(false)
      val base = if (active) List("item", "item--active") else List("item")
      if (item.model.done) "item--done" :: base else base
    }

    item match {
      case Right(item) =>
        val checkboxId = s"checkbox-${item.id}"
        val className = getClasses(item).mkString(" ")
        <div class={className} onclick={openItem(item)(_)}>
          <span onclick={onChange}><input id={checkboxId} type="checkbox" checked={conditionalAttribute(item.model.done)}></input> </span>
          <span>{item.model.title}</span>
          <span class="item__create-date"> {formatCreateDate(item.model.createDate)} </span>
        </div>
      case Left(err) =>
        <div class="item--error">{err}</div>
    }
  }

  def openItemNode(item: Option[Item.Stored]): xml.Node = item match {
    case Some(i) =>
      <dl class="item-info">
        <dt>ID</dt><dd>{i.id}</dd>
        <dt>Title</dt><dd>{i.model.title}</dd>
        <dt>Created</dt><dd>{i.model.createDate.toString}</dd>
        <dt>Notes</dt><dd><textarea></textarea></dd>
      </dl>
    case None => <div></div>
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

