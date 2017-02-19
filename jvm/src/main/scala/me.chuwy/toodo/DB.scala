package me.chuwy.toodo

import fs2._

import doobie.imports._

import Data.Item

object DB {

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:toodo", "chuwy", "Supersecret1"
  )

  def allItems: Task[List[Item]] = {
    val sql: Query0[Item] = sql"select title, create_date from items".query[Item]
    sql.list.transact(xa)
  }

  def insertItem(item: Item): Task[Int] = {
    val sql: Update0 = sql"INSERT INTO items (title, done, create_date) VALUES (${item.title}, FALSE, ${item.createDate})".update
    sql.run.transact(xa)
  }
}

