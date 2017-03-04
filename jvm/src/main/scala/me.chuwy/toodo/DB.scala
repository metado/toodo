package me.chuwy.toodo

import java.sql.Timestamp

import java.time.LocalDateTime
import java.time.{ ZoneId, ZoneOffset }

import fs2._
import fs2.interop.cats._

import doobie.imports._
import doobie.free.connection._

import cats.syntax.all._
import cats.instances.all._

import Data._

object DB {

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:toodo", "chuwy", "Supersecret1"
  )

  def convertLocalDateTime(bpTime: LocalDateTime): Timestamp =
    new Timestamp(bpTime.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)

  def convertTimestamp(jst: Timestamp): LocalDateTime =
    LocalDateTime.ofEpochSecond(jst.getTime / 1000, 0, ZoneOffset.UTC)

  implicit val localDateTimeMeta =
    Meta[Timestamp].xmap(convertTimestamp, convertLocalDateTime)

  def allItems: Task[List[Item.Stored]] = {
    val sql: Query0[Item.Stored] = sql"SELECT id, title, done, create_date FROM items".query[Item.Stored]
    sql.list.transact(xa)
  }

  def getItem(id: Int): Task[Item.Stored] = {
    val sql: Query0[Item.Stored] = sql"SELECT id, title, done, create_date FROM items WHERE id = $id".query[Item.Stored]
    sql.unique.transact(xa)
  }

  def insertItem(item: Item): Task[Item.Stored] = {
    val sql: Update0 = sql"INSERT INTO items (title, done, create_date) VALUES (${item.title}, ${item.done}, ${item.createDate})".update
    val insterted = sql.withUniqueGeneratedKeys[Item.Stored]("id", "title", "done", "create_date")
    insterted.transact(xa)
  }

  val fixtures = List(
    Item("Do something"),
    Item("Write software"),
    Item("Do my job"),
    Item("Do nothing"),
    Item("Sleep"),
    Item("Go to gym"),
    Item("Read some books")
  )

}

