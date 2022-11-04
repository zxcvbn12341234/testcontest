package testcontest.model

import io.circe.generic.semiauto._
import io.circe._

import java.util.UUID

case class Participant(
    id: UUID,
    contestId: UUID,
    username: String,
    answers: Map[UUID, Short]
)

object Participant {

  def participant(
      contestId: UUID,
      username: String,
      userAnswers: Map[UUID, Short]
  ): Participant =
    Participant(UUID.randomUUID(), contestId, username, userAnswers)

  trait JsonSupport {
    implicit val ParticipantJsonEncoder: Encoder[Participant] =
      deriveEncoder[Participant]
    implicit val ParticipantJsonDecoder: Decoder[Participant] =
      deriveDecoder[Participant]
  }

  object JsonSupport extends JsonSupport

}
