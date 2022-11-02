package testcontest.model

import io.circe._
import io.circe.generic.semiauto._

import java.util.UUID

case class Participant(
    participantId: UUID,
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

  object JsonSupport {
    implicit val ParticipantJsonEncoder: Encoder[Participant] =
      deriveEncoder[Participant]
    implicit val ParticipantJsonDecoder: Decoder[Participant] =
      deriveDecoder[Participant]
  }

}
