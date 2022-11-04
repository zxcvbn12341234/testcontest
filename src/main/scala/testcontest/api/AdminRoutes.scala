package testcontest.api

import cats.effect._
import cats.implicits._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import testcontest.services._

import testcontest.model.JsonSupport._

final class AdminRoutes[F[_]: JsonDecoder: Async](
    contestRepository: ContestRepository[F],
    userRepository: UserRepository[F],
    participantRepository: ParticipantRepository[F],
    questionRepository: QuestionRepository[F],
) {

  def allRoutes: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    // TODO: How to add "admin" prefix to all routes here?
    HttpRoutes.of[F] {
      case GET -> Root / "admin" / "contest" =>
        contestRepository.getAllContests
          .flatMap(contests => Ok(contests.asJson))

      case GET -> Root / "admin" / "user" =>
        userRepository.getAllUsers
          .flatMap(users => Ok(users.asJson))

      case GET -> Root / "admin" / "participant" =>
        participantRepository.getAllParticipants
          .flatMap(participants => Ok(participants.asJson))

      case GET -> Root / "admin" / "question" =>
        questionRepository.getAllQuestions
          .flatMap(questions => Ok(questions.asJson))
    }
  }
}
