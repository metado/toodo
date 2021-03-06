package me.chuwy.toodo

import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext

import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{ HTMLInputElement, HTMLTextAreaElement }

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.circe._, time._, generic.auto._

import Data.Item
import Model.accept

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
      { Model.getState.map(_.openItem).map(View.itemInfoNode) }
    </div>
  }

  def itemNode(item: Either[String, Item.Stored]): xml.Node = {
    val onChange: (dom.MouseEvent) => Unit = { _: dom.MouseEvent =>
      item match {
        case Right(i) =>
          val newItem = Data.Item.doneLens.set(i)(!i.model.done)
          Controller.updateItem(newItem).onComplete {
            case Failure(err) =>
              dom.console.log(err.asInstanceOf[js.Any])
            case Success(_) =>
              accept(Model.UpdateItem(newItem))
          }
        case _ => ()
      }
    }

    def getClasses(item: Item.Stored): List[String] = {
      val openItem = Model.getState.map(_.openItem).impure.value
      val active = openItem.map(o => o.id == item.id).getOrElse(false)
      val base = if (active) List("item", "item--active") else List("item")
      if (item.model.done) "item--done" :: base else base
    }

    item match {
      case Right(item) =>
        val checkboxId = s"checkbox-${item.id}"
        val className = getClasses(item).mkString(" ")
        <div class={className} onclick={(e: dom.MouseEvent) => accept(Model.SelectItem(item))}>
          <span onclick={onChange}><input id={checkboxId} type="checkbox" checked={conditionalAttribute(item.model.done)}></input> </span>
          <span>{item.model.title}</span>
          <span class="item__create-date"> {formatCreateDate(item.model.createDate)} </span>
        </div>
      case Left(err) =>
        <div class="item--error">{err}</div>
    }
  }

  def itemInfoNode(item: Option[Item.Stored]): xml.Node = {
    item match {
      case Some(i) =>
        val onInputKeydown: (dom.KeyboardEvent) => Unit = { event: dom.KeyboardEvent =>
          (event.currentTarget, event.keyCode) match {
            case (input: HTMLTextAreaElement, KeyCode.Enter) =>
              input.value.trim match {
                case note if note.length > 1 =>
                  val newItem = Data.Item.noteLens.set(i)(Some(note))
                  Controller.updateItem(newItem).onComplete {
                    case Success(_) =>
                      accept(Model.UpdateItem(newItem))
                      accept(Model.SelectItem(newItem))
                    case Failure(f) => println("Failure " + f.toString)
                  }
                case _ => ()
              }
            case _ => ()
          }
        }

        <dl class="item-info">
          <div><span style="color: #aaaaaa">#{i.id}</span> {i.model.title}</div>
          <div>{i.model.createDate.toString}</div>

          <dt>Notes</dt>
          <dd>
            <textarea onkeydown={onInputKeydown}>{i.model.note.getOrElse("")}</textarea>
            <button>Update</button>
          </dd>
        </dl>
      case None => <div></div>
    }
  }

  val newItemNode: xml.Node = {
    val onInputKeydown: (dom.KeyboardEvent) => Unit = { event: dom.KeyboardEvent =>
      (event.currentTarget, event.keyCode) match {
        case (input: HTMLInputElement, KeyCode.Enter) =>
          input.value.trim match {
            case "" =>
            case title =>
              Controller.createItem(Item(title)).map(Model.parseXhr[Item.Stored]).onComplete {
                case Success(item) =>
                  accept(Model.AddItem(item))
                case Failure(fail) =>
                  dom.console.log(fail.asInstanceOf[js.Any])
              }
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

