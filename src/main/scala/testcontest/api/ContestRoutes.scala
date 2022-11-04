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
import testcontest.model.Contest.JsonSupport._
import testcontest.model.Participant.JsonSupport._
import testcontest.services.ContestService

import java.time.OffsetDateTime
import java.util.UUID
import scala.util.{Failure, Success, Try}

final class ContestRoutes[F[_]: JsonDecoder: Async](
    contestService: ContestService[F]
) {

  import ContestRoutes._

  def contestRoutes: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {

      // Create a new empty contest
      case POST -> Root / "contest" =>
        contestService.createEmptyContest
          .flatMap(contest => Ok(contest.asJson))

      // Get a contest
      case GET -> Root / "contest" / UUIDVar(contestId) =>
        contestService
          .getContest(contestId)
          .flatMap {
            case Some(contest) => Ok(contest.asJson)
            case None          => NotFound(s"Contest with $contestId is not found")
          }

      // Modify 'startDate' and 'endDate' of an existing contest
      case req @ POST -> Root / "contest" / UUIDVar(contestId) / "dates" =>
        req
          .decodeJson[ContestDatesUpdateRequest]
          .flatMap(
            request =>
              contestService.updateContestDates(
                contestId,
                request.startDate,
                request.endDate
            ))
          .flatMap(_ => contestService.getContest(contestId))
          .flatMap {
            case Some(contest) => Ok(contest.asJson)
            case None =>
              NotFound(s"Could not find contest with contest id $contestId")
          }

      // Append some list of questions to an already existing contest
      case req @ POST -> Root / "contest" / UUIDVar(
            contestId
          ) / "add-questions" =>
        req
          .decodeJson[ContestAddQuestionsRequest]
          .flatMap(request =>
            request.questions.traverse(question => contestService.addQuestion(contestId, question)))
          .flatMap(_ => contestService.getContest(contestId))
          .flatMap {
            case Some(contest) => Ok(contest.asJson)
            case None =>
              NotFound(s"Could not find contest with contest id $contestId")
          }
    }
  }

  def participantRoutes: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {

      // Append some list of participants to an already existing contest
      // It is enough to just send a 'userId'
      // Participant object itself will be created automatically
      // fixme: Same user sent multiple times, more than one participant will be created each time
      case req @ POST -> Root / "contest" / UUIDVar(
            contestId
          ) / "add-participants" =>
        req
          .decodeJson[ContestAddParticipantsRequest]
          .flatMap(request =>
            request.users.traverse(username => contestService.addParticipant(contestId, username)))
          .flatMap(_ => contestService.getContest(contestId))
          .flatMap {
            case Some(contest) => Ok(contest.asJson)
            case None =>
              NotFound(s"Could not find contest with contest id $contestId")
          }

      case req @ POST -> Root / "contest" / UUIDVar(
            contestId
          ) / "put-answers" =>
        req
          .decodeJson[ContestParticipantPutAnswersRequest]
          .flatMap(request =>
            request.answers.toList.traverse {
              case (questionId, answer) =>
                contestService
                  .registerAnswer(
                    contestId,
                    request.username,
                    questionId,
                    answer
                  )
            }.map(participants => Try(participants.last)))
          .flatMap {
            case Success(participant) => Ok(participant.asJson)
            case Failure(_) =>
              NotFound("Could not find resources for the request")
          }
          .handleErrorWith {
            case ex: Exception =>
              InternalServerError(ex.toString)
          }

      case GET -> Root / "contest" / UUIDVar(
            contestId
          ) / "participant" / username =>
        contestService
          .getResult(contestId, username)
          .flatMap(correctCount => Ok(s"User has $correctCount correct answers"))
    }
  }

  def allRoutes: HttpRoutes[F] = {
    contestRoutes <+> participantRoutes
  }

}

object ContestRoutes {
  case class ContestDatesUpdateRequest(
      startDate: OffsetDateTime,
      endDate: OffsetDateTime
  )
  implicit val CDatesUpdateRequestEncoder: Decoder[ContestDatesUpdateRequest] =
    deriveDecoder[ContestDatesUpdateRequest]

  case class ContestAddQuestionsRequest(questions: List[UUID])
  implicit val CAddQuestionsRequestEncoder: Decoder[ContestAddQuestionsRequest] =
    deriveDecoder[ContestAddQuestionsRequest]

  case class ContestAddParticipantsRequest(users: List[String])
  implicit val CAddParticipantsRequestEncoder: Decoder[ContestAddParticipantsRequest] =
    deriveDecoder[ContestAddParticipantsRequest]

  case class ContestParticipantPutAnswersRequest(
      username: String,
      answers: Map[UUID, Short]
  )
  implicit val CParticipantPutAnswersRequestEncoder: Decoder[ContestParticipantPutAnswersRequest] =
    deriveDecoder[ContestParticipantPutAnswersRequest]
}
