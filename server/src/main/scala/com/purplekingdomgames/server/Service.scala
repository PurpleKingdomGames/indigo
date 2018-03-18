package com.purplekingdomgames.server

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import io.circe.generic.auto._
import io.circe.syntax._
import java.io.File

import fs2.interop.cats._

object Service {
  val service = HttpService {

    case GET -> Root / "ping" =>
      Ok("pong").replaceAllHeaders(Header("Access-Control-Allow-Origin", "*"), Header("Content-Type", "text/plain"))

    case GET -> Root / "game" / "id" / "definition" =>
      Ok(GameDetails.definition.asJson)

    case GET -> Root / "game" / "id" / "config" =>
      Ok(GameDetails.config.asJson)

    case GET -> Root / "game" / "id" / "assets" =>
      Ok(GameDetails.assets.asJson)

    case request @ GET -> Root / "game" / "id" / "assets" / path =>
      StaticFile.fromFile(new File("./server/assets/" + path), Some(request))
        .getOrElseF(NotFound())

  }
}
