package testcontest.model

import io.circe._
import io.circe.generic.semiauto._

import java.time.OffsetDateTime
import java.util.UUID

case class Contest(
    id: UUID,
    participants: List[UUID],
    questions: List[UUID],
    startDate: OffsetDateTime,
    endDate: OffsetDateTime
)

object Contest {

  def contest(
      participants: List[UUID],
      questions: List[UUID],
      startDate: OffsetDateTime,
      endDate: OffsetDateTime
  ): Contest =
    Contest(UUID.randomUUID(), participants, questions, startDate, endDate)

  trait JsonSupport {
    implicit val ContestJsonEncoder: Encoder[Contest] =
      deriveEncoder[Contest]
    implicit val ContestJsonDecoder: Decoder[Contest] =
      deriveDecoder[Contest]

    import io.circe.syntax._
    implicit val uuidEncoder: Encoder[UUID] = uuid => uuid.toString.asJson
  }
  object JsonSupport extends JsonSupport

}
