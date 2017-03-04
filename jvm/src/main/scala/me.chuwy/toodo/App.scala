package me.chuwy.toodo

import java.nio.file._
import java.sql.Timestamp

import cats._, data._, instances.all._, syntax.all._

import io.circe._, generic.auto._, parser._, io.circe.syntax._, io.circe.time._

import fs2._

import org.http4s.{ParsingFailure => _, _}
import dsl._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s.circe._

// This project
import Data._

object App extends ServerApp {

  implicit val dtime: Decoder[java.sql.Timestamp] =
    Decoder.const[java.sql.Timestamp](new Timestamp(12312312312L))

  implicit val etime: Encoder[java.sql.Timestamp] =
    Encoder.instance((t: java.sql.Timestamp) => Json.fromString(t.getTime.toString))

  override def server(args: List[String]): Task[Server] = {

    /**
      * Mount point for static assets
      */
    val index = HttpService {
      case GET -> Root =>
        lazy val byteStream = Thread.currentThread.getContextClassLoader.getResourceAsStream("index.html")
        val index = io.readInputStream(Task.delay(byteStream), 256 * 2)
        val mimeType = Headers(headers.`Content-Type`(MediaType.`text/html`))
        val response = Response(Status.Ok, body = index, headers = mimeType)
        Task.delay(response)

      case GET -> Root / "cross-fastopt.js" =>
        val index = io.readInputStream(Task.delay {
          Thread.currentThread.getContextClassLoader.getResourceAsStream("content/target/cross-fastopt.js")
        }, 2048 * 1024) // Don't know why it truncates my files
        val mimeType = Headers(headers.`Content-Type`(MediaType.`application/json`))
        val response = Response(Status.Ok, body = index, headers = mimeType)
        Task.delay(response)


      case GET -> Root / "cross-fastopt.js.map" =>
        val mimeType = Headers(headers.`Content-Type`(MediaType.`application/javascript`))
        val index = io.readInputStream(Task.delay {
          Thread.currentThread.getContextClassLoader.getResourceAsStream("content/target/cross-fastopt.js.map")
        }, 2048 * 1024) // Don't know why it truncates my files
        val response = Response(Status.Ok, body = index, headers = mimeType)
        Task.delay(response)
    }

    /**
      * Mount point for toodo REST API
      */
    val todoCrud = HttpService {
      case GET -> Root =>
        for {
          allItems <- DB.allItems.map(_.asJson)
          response <- Ok(allItems)
        } yield response

      case GET -> Root / "item" / IntVar(itemId) =>
        for {
          item <- DB.getItem(itemId)
          response <- Ok(item.asJson)
        } yield response

      case request @ POST -> Root / "item" =>
        for {
          item <- request.as(jsonOf[Item])
          inserted <- DB.insertItem(item)
          uri = Uri.unsafeFromString(s"http://127.0.0.1/api${Data.Item.getUri(inserted)}")
          response <- Created(inserted.asJson).putHeaders(headers.Location(uri))
        } yield response

      case GET -> Root / "file" / name =>
        Task.delay { Response(Status.Ok, body = Experimenting.readFile(name)) }
    }

    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(index, "/")
      .mountService(todoCrud, "/api")
      .start
  }
}

object Experimenting {
  def readFile(path: String): Stream[Task, Byte] = {
    val content = Files.newInputStream(Paths.get(s"/Users/chuwy/$path"))
    io.readInputStream(Task.delay(content), 32)
  }
}
