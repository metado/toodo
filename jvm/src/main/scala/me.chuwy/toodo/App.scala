package me.chuwy.toodo

import java.nio.file._

import cats._, data._, instances.all._, syntax.all._

import io.circe._, generic.auto._, parser._, io.circe.syntax._

import io.circe.time._

import fs2._

import org.http4s.{ ParsingFailure => _, _}, dsl._
import org.http4s.server.{ ServerApp, Server }
import org.http4s.server.blaze._
import org.http4s.circe._

// This project
import Data._

object App extends ServerApp {

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

      case GET -> Root / "app.js" =>
        val index = io.readInputStream(Task.delay {
          Thread.currentThread.getContextClassLoader.getResourceAsStream("content/target/cross-fastopt.js")
        }, 2048 * 1024) // Don't know why it truncates my files
        val mimeType = Headers(headers.`Content-Type`(MediaType.`application/javascript`))
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

      case request @ POST -> Root / "item" =>
        for {
          item <- request.as(jsonOf[Item])
          inserted <- DB.insertItem(item)
          response <- Ok(inserted.asJson)
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
