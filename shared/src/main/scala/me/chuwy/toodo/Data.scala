package me.chuwy.toodo

import java.time.LocalDateTime

object Data {

  case class WithId[A](id: Long, model: A)

  case class Item(title: String, done: Boolean, createDate: LocalDateTime)

  type IdItem = WithId[Item]

  object Item {
    def apply(title: String): Item =
      Item(title, false, LocalDateTime.now)
  }
}
