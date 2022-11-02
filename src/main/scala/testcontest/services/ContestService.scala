package testcontest.services

import testcontest.model.{Contest, Participant}
import cats._
import cats.syntax.all._

import java.time.OffsetDateTime
import java.util.UUID

trait ContestService[F[_]] {

  def createEmptyContest: F[Contest]

  def getContest(contestId: UUID): F[Option[Contest]]

  def updateContestDates(
      contestId: UUID,
      startDate: OffsetDateTime,
      endDate: OffsetDateTime
  ): F[Unit]

  def addQuestion(contestId: UUID, question: UUID): F[Unit]
  def deleteQuestion(contestId: UUID, question: UUID): F[Unit]

  def addParticipant(contestId: UUID, user: String): F[Unit]
  def deleteParticipant(contestId: UUID, participant: UUID): F[Unit]

  def registerAnswer(
      contestId: UUID,
      username: String,
      questionId: UUID,
      userAnswer: Short
  ): F[Participant]

  // Currently returns number of correct answers
  def getResult(contestId: UUID, username: String): F[Int]

  // TODO: need to figure out how to represent
  def getLeaderboard: F[Unit]

  def getAllParticipants: F[List[Participant]]
}

object ContestService {

  def make[F[_]](
      contestRepository: ContestRepository[F],
      participantRepository: ParticipantRepository[F],
      questionRepository: QuestionRepository[F]
  )(implicit ev: MonadError[F, Throwable]): F[ContestService[F]] =
    Monad[F].pure {
      new ContestService[F] {
        override def createEmptyContest: F[Contest] = {
          val contest = Contest.contest(
            List.empty,
            List.empty,
            OffsetDateTime.now(),
            OffsetDateTime.now()
          )
          contestRepository.putContest(contest)
        }

        override def getContest(contestId: UUID): F[Option[Contest]] =
          contestRepository.getContest(contestId)

        private def updateContest(
            contestId: UUID,
            f: Contest => Contest
        ): F[Unit] = {
          contestRepository.getContest(contestId).flatMap {
            case Some(contest) =>
              val updatedContest = f(contest)
              contestRepository.putContest(updatedContest).map(_ => ())
            case None => Monad[F].unit
          }
        }

        override def updateContestDates(
            contestId: UUID,
            startDate: OffsetDateTime,
            endDate: OffsetDateTime
        ): F[Unit] = updateContest(
          contestId,
          contest => contest.copy(startDate = startDate, endDate = endDate)
        )

        override def addQuestion(contestId: UUID, question: UUID): F[Unit] =
          updateContest(
            contestId,
            contest => contest.copy(questions = contest.questions :+ question)
          )

        override def deleteQuestion(
            contestId: UUID,
            question: UUID
        ): F[Unit] =
          updateContest(
            contestId,
            contest =>
              contest.copy(questions = contest.questions.filter(_ != question))
          )

        override def addParticipant(
            contestId: UUID,
            user: String
        ): F[Unit] =
          participantRepository
            .putParticipant(
              Participant.participant(contestId, user, Map.empty)
            )
            .flatMap(participant =>
              updateContest(
                contestId,
                contest =>
                  contest
                    .copy(participants =
                      contest.participants :+ participant.participantId
                    )
              )
            )

        override def deleteParticipant(
            contestId: UUID,
            participant: UUID
        ): F[Unit] =
          updateContest(
            contestId,
            contest =>
              contest.copy(participants =
                contest.participants.filter(_ == participant)
              )
          ).flatMap(_ => participantRepository.deleteParticipant(participant))

        override def registerAnswer(
            contestId: UUID,
            username: String,
            questionId: UUID,
            userAnswer: Short
        ): F[Participant] =
          participantRepository.getParticipant(contestId, username).flatMap {
            case Some(participant) =>
              val updatedParticipant = participant.copy(answers =
                  participant.answers.updated(questionId, userAnswer)
                )
              participantRepository.putParticipant(updatedParticipant)
            case None => ev.raiseError(new Exception("Participant not found"))
          }

        // TODO: Check if we can use OptionT here
        override def getResult(contestId: UUID, username: String): F[Int] = for {
          participantMaybe <- participantRepository.getParticipant(
            contestId,
            username
          )
          participant = participantMaybe.get

          contestMaybe <- contestRepository.getContest(participant.contestId)
          contest = contestMaybe.get

          questions <- contest.questions
            .traverse(questionId => questionRepository.getQuestion(questionId))
            .map(_.map(_.get))

          score = questions
            .map(question =>
              participant.answers.get(question.id) match {
                case Some(answer) if answer == question.correctAnswer => 1
                case _                                                => 0
              }
            )
            .sum

        } yield score

        override def getLeaderboard: F[Unit] = Monad[F].unit

        override def getAllParticipants: F[List[Participant]] = participantRepository.getAllParticipants()
      }
    }
}
