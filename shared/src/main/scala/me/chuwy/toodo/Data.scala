package me.chuwy.toodo

import java.time.LocalDateTime

object Data {

  case class WithId[A](id: Long, model: A)

  case class Item(title: String, done: Boolean, createDate: LocalDateTime)

  object Item {

    type Stored = WithId[Item]

    def getUri(item: Stored): String =
      s"/item/${item.id}"

    def apply(title: String): Item =
      Item(title, false, LocalDateTime.now)
  }
}
