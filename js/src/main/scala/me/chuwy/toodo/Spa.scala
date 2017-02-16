package me.chuwy.toodo

import scala.scalajs.js

import Data.Item

object Spa extends js.JSApp {
  def main(): Unit = {
    val item = Item("Write this app", "sooner or later!")
    println(s"A lot of things need to be done. This is one of them $item")
  }
}
