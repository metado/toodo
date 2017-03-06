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
    Meta[Timestamp].nxmap(convertTimestamp, convertLocalDateTime)

  def allItems: Task[List[Item.Stored]] = {
    val sql: Query0[Item.Stored] = sql"SELECT id, title, done, create_date, note, start_time, end_time FROM items".query[Item.Stored]
    sql.list.transact(xa)
  }

  def getItem(id: Int): Task[Item.Stored] = {
    val sql: Query0[Item.Stored] = sql"SELECT id, title, done, create_date, note, start_time, end_time FROM items WHERE id = $id".query[Item.Stored]
    sql.unique.transact(xa)
  }

  def updateItem(item: Item.Stored): Task[Int] = {
    val sql: Update0 =
      (fr"UPDATE items SET " ++
        fr"title = ${item.model.title}, " ++
        fr"done = ${item.model.done}, " ++
        fr"create_date = ${item.model.createDate}, " ++
        fr"note = ${item.model.note}, " ++
        fr"start_time = ${item.model.startTime}, " ++
        fr"end_time = ${item.model.endTime} " ++
        fr"WHERE id = ${item.id}").update
    sql.run.transact(xa)
  }

  def insertItem(item: Item): Task[Item.Stored] = {
    val sql: Update0 = sql"INSERT INTO items (title, done, create_date, note, start_date, end_date) VALUES (${item.title}, ${item.done}, ${item.createDate}, ${item.note}, ${item.startTime}, ${item.endTime})".update
    val insterted = sql.withUniqueGeneratedKeys[Item.Stored]("id", "title", "done", "create_date", "note", "start_time", "end_time")
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

