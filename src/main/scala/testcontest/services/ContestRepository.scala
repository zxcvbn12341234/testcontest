package testcontest.services

import cats.effect.std.Console
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model.Contest
import testcontest.model.Contest.JsonSupport._

import java.util.UUID

trait ContestRepository[F[_]] {

  def getContest(contestId: UUID): F[Option[Contest]]
  def putContest(contest: Contest): F[Contest]
  def deleteContest(contestId: UUID): F[Unit]

  def addQuestion(contestId: UUID, questionId: UUID): F[Unit]
  def deleteQuestion(contestId: UUID, questionId: UUID): F[Unit]

  def addParticipant(contestId: UUID, participantId: UUID): F[Unit]
  def deleteParticipant(contestId: UUID, participantId: UUID): F[Unit]

  def getAllContests: F[List[Contest]]

  def start: F[Unit]
  def finished: F[Unit]

}

object ContestRepository {

  def makeInMemory[F[_]: Sync: Console]: F[ContestRepository[F]] =
    Ref.of[F, Map[UUID, Contest]](Map.empty[UUID, Contest]).map { contestDatabaseF =>
      new ContestRepository[F] with BaseRepository[F, UUID, Contest] {

        override val storageRef: Ref[F, Map[UUID, Contest]] = contestDatabaseF
        override val repositoryName                         = "contest-repository"

        override def key(value: Contest) = value.id

        override def getContest(contestId: UUID): F[Option[Contest]] =
          get(contestId)

        override def putContest(contest: Contest): F[Contest] =
          put(contest)

        override def deleteContest(contestId: UUID): F[Unit] =
          delete(contestId)

        override def getAllContests: F[List[Contest]] = getAll

        override def addQuestion(
            contestId: UUID,
            questionId: UUID
        ): F[Unit] = update(
          contestId,
          contest => contest.copy(questions = contest.questions :+ questionId)
        )

        override def deleteQuestion(
            contestId: UUID,
            questionId: UUID
        ): F[Unit] = update(
          contestId,
          contest =>
            contest
              .copy(questions = contest.questions.filter(_ != questionId))
        )

        override def addParticipant(
            contestId: UUID,
            participantId: UUID
        ): F[Unit] = update(
          contestId,
          contest => contest.copy(participants = contest.participants :+ participantId)
        )

        override def deleteParticipant(
            contestId: UUID,
            participantId: UUID
        ): F[Unit] = update(
          contestId,
          contest =>
            contest
              .copy(participants = contest.participants.filter(_ != participantId))
        )

        override def start: F[Unit] =
          Console[F].println("Initializing contest repository from file...") >>
            readSnapshot >>
            Console[F].println("Initialized.")

        override def finished: F[Unit] =
          Console[F].println("Saving contest repository file...") >>
            saveSnapshot >>
            Console[F].println("Saved.")
      }
    }
}
