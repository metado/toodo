package me.chuwy.toodo

import java.time.LocalDateTime

import shapeless._

object Data {

  val endpoint = "http://127.0.0.1:8080/api"

  case class WithId[A](id: Long, model: A)

  case class Item(
    title: String,
    done: Boolean,
    createDate: LocalDateTime,
    note: Option[String],
    startTime: Option[LocalDateTime],
    endTime: Option[LocalDateTime])

  object Item {

    val path = "item"
    val endpoint = s"${Data.endpoint}/$path"

    type Stored = WithId[Item]

    val modelLens = lens[Stored] >> 'model
    val doneLens  = lens[Stored] >> 'model >> 'done
    val noteLens  = lens[Stored] >> 'model >> 'note

    def getUri(item: Stored): String =
      s"/$path/${item.id}"

    def getAbsoluteUri(item: Stored): String =
      s"$endpoint/${item.id}"

    def apply(title: String): Item =
      Item(title, false, LocalDateTime.now, None, None, None)
  }
}
