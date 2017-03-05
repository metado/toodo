package me.chuwy.toodo

import java.sql.Timestamp

import scala.io.Source

import io.circe._, generic.auto._, io.circe.syntax._, io.circe.time._

import fs2._

import org.http4s.{ParsingFailure => _, _}
import dsl._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s.circe._

// This project
import Data._

object App extends ServerApp {

  object WholePathVar {
    def unapply(path: Path): Option[String] =
      Some(path.toList.mkString("/"))
  }

  implicit val dtime: Decoder[java.sql.Timestamp] =
    Decoder.const[java.sql.Timestamp](new Timestamp(12312312312L))

  implicit val etime: Encoder[java.sql.Timestamp] =
    Encoder.instance((t: java.sql.Timestamp) => Json.fromString(t.getTime.toString))

  def getMime(filename: String): Header = {
    val extension = filename.split('.').last
    val mediaType = MediaType.forExtension(extension).getOrElse(MediaType.`text/plain`)
    headers.`Content-Type`(mediaType)
  }


  override def server(args: List[String]): Task[Server] = {

    /**
      * Mount point for static assets
      */
    val index = HttpService {
      case GET -> Root =>
        val byteStream = Thread.currentThread.getContextClassLoader.getResourceAsStream("index.html")
        val index = Source.fromInputStream(byteStream).mkString
        val mimeType = headers.`Content-Type`(MediaType.`text/html`)
        Ok().withBody(index).putHeaders(mimeType)
      case GET -> path =>
        val filePath = path.toList.mkString("/")
        Option(Thread.currentThread.getContextClassLoader.getResourceAsStream(filePath)) match {
          case Some(byteStream) =>
            val body = Source.fromInputStream(byteStream).mkString
            val mimeType = getMime(filePath)
            Ok().withBody(body).putHeaders(mimeType)
          case None =>
            NotFound()
        }
    }

    /**
      * Mount point for toodo REST API
      */
    val todoCrud = HttpService {
      case GET -> Root / "item" =>
        for {
          allItems <- DB.allItems.map(_.asJson)
          response <- Ok(allItems)
        } yield response

      case GET -> Root / "item" / IntVar(itemId) =>
        for {
          item <- DB.getItem(itemId)
          response <- Ok(item.asJson)
        } yield response

      case request @ PUT -> Root / "item" / IntVar(itemId) =>
        for {
          item <- request.as(jsonOf[Item.Stored])
          _ <- DB.updateItem(item)
          response <- Ok(item.asJson)
        } yield response

      case request @ POST -> Root / "item" =>
        for {
          item <- request.as(jsonOf[Item])
          inserted <- DB.insertItem(item)
          uri = Uri.unsafeFromString(Item.getAbsoluteUri(inserted))
          response <- Created(inserted.asJson).putHeaders(headers.Location(uri))
        } yield response
    }

    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(index, "/")
      .mountService(todoCrud, "/api")
      .start
  }
}
