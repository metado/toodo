package me.chuwy.toodo

import org.threeten.bp.LocalDateTime

object Data {
  case class Item(title: String, done: Boolean, createDate: LocalDateTime)
}
