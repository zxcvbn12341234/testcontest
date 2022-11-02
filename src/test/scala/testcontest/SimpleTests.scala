package testcontest

import cats.effect.IO
import munit.CatsEffectSuite
import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import testcontest.api.ContestRoutes.ContestParticipantPutAnswersRequest

import java.util.UUID

class SimpleTests extends CatsEffectSuite {

  test("need some tests...") {
    val xd = testcontest.api.ContestRoutes.CParticipantPutAnswersRequestEncoder
    val expected = ContestParticipantPutAnswersRequest(
      "asdas",
      Map(UUID.fromString("c195d0c2-b64a-4788-bba2-99fd87e3024f") -> 2)
    )
    implicit val xdeee = deriveEncoder[ContestParticipantPutAnswersRequest]

    val json =
      """{ "username": "asdas", "answers": { "c195d0c2-b64a-4788-bba2-99fd87e3024f": 2 } }"""
    val jsjsj =
    for {
      _ <- IO.println(jsjsj)
      _ <- IO.println(expected.asJson)
      _ <- IO.println(xd.decodeJson(jsjsj))
    } yield ()
  }
}
