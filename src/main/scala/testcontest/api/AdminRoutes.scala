package testcontest.api

import cats.effect._
import cats.implicits._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.server.Router
import testcontest.services._
import testcontest.model.JsonSupport._

final class AdminRoutes[F[_]: JsonDecoder: Async](
    contestRepository: ContestRepository[F],
    userRepository: UserRepository[F],
    participantRepository: ParticipantRepository[F],
    questionRepository: QuestionRepository[F],
) {

  private def adminRoutes: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "contest" =>
        contestRepository.getAllContests
          .flatMap(contests => Ok(contests.asJson))

      case GET -> Root / "user" =>
        userRepository.getAllUsers
          .flatMap(users => Ok(users.asJson))

      case GET -> Root / "participant" =>
        participantRepository.getAllParticipants
          .flatMap(participants => Ok(participants.asJson))

      case GET -> Root / "question" =>
        questionRepository.getAllQuestions
          .flatMap(questions => Ok(questions.asJson))
    }
  }
  def allRoutes: HttpRoutes[F] = Router("admin" -> adminRoutes)
}
