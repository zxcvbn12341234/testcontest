package testcontest.model

import io.circe._
import io.circe.generic.semiauto._

import java.util.UUID

case class Question(
    id: UUID,
    description: String,
    answers: List[String],
    correctAnswer: Short
)

object Question {
  def question(
      description: String,
      answers: List[String],
      correctAnswer: Short
  ): Question = Question(UUID.randomUUID(), description, answers, correctAnswer)

  object JsonSupport {
    implicit val QuestionJsonEncoder: Encoder[Question] =
      deriveEncoder[Question]
    implicit val QuestionJsonDecoder: Decoder[Question] =
      deriveDecoder[Question]
  }

}
