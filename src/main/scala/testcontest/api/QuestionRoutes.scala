package testcontest.api

import cats.effect.Async
import cats.syntax.all._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl._
import testcontest.model.Question.JsonSupport._
import testcontest.services.QuestionService

import java.util.UUID

final class QuestionRoutes[F[_]: JsonDecoder: Async](
    questionService: QuestionService[F]
) {

  import QuestionRoutes._

  def questionRoutes: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      // Get all questions
      case GET -> Root / "question" =>
        questionService.allQuestions
          .flatMap(questions => Ok(questions.asJson))

      // Get a particular question
      case GET -> Root / "question" / UUIDVar(questionId) =>
        questionService
          .getQuestion(questionId)
          .flatMap {
            case Some(question) => Ok(question.asJson)
            case None           => NotFound()
          }

      // Add a new question
      case req @ (POST -> Root / "question") =>
        req
          .decodeJson[QuestionCreateRequest]
          .flatMap(
            request =>
              questionService
                .addQuestion(
                  request.description,
                  request.answers,
                  request.correctAnswer
              ))
          .flatMap(question => Ok(question.asJson))
          .handleErrorWith {
            case InvalidMessageBodyFailure(details, _) =>
              BadRequest(s"Failed to deserialize user\n$details")
          }
    }
  }

  def allRoutes: HttpRoutes[F] = {
    questionRoutes
  }

}

object QuestionRoutes {
  case class QuestionCreateRequest(
      description: String,
      answers: List[String],
      correctAnswer: Short
  )
  implicit val QuestionCreateRequestEncoder: Decoder[QuestionCreateRequest] =
    deriveDecoder[QuestionCreateRequest]
}
