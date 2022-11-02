package testcontest.services

import cats._
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model.{Contest, Question}

import java.util.UUID

trait ContestRepository[F[_]] {

  def getContest(contestId: UUID): F[Option[Contest]]
  def putContest(contest: Contest): F[Contest]
  def deleteContest(contestId: UUID): F[Unit]

  def addQuestion(contestId: UUID, questionId: UUID): F[Unit]
  def deleteQuestion(contestId: UUID, questionId: UUID): F[Unit]

  def addParticipant(contestId: UUID, participantId: UUID): F[Unit]
  def deleteParticipant(contestId: UUID, participantId: UUID): F[Unit]

}

object ContestRepository {

  def makeInMemory[F[_]: Sync](implicit
      ev: ApplicativeError[F, Throwable]
  ): F[ContestRepository[F]] =
    Ref.of[F, Map[UUID, Contest]](Map.empty[UUID, Contest]).map {
      contestDatabaseF =>
        new ContestRepository[F] with BaseRepository[F, Contest] {
          override val storageRef: Ref[F, Map[UUID, Contest]] = contestDatabaseF
          override implicit val ev: Applicative[F] = implicitly[Applicative[F]]

          override def getContest(contestId: UUID): F[Option[Contest]] =
            get(contestId)

          override def putContest(contest: Contest): F[Contest] =
            put(contest.id, contest)

          override def deleteContest(contestId: UUID): F[Unit] =
            delete(contestId)

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
            contest =>
              contest.copy(participants = contest.participants :+ participantId)
          )

          override def deleteParticipant(
              contestId: UUID,
              participantId: UUID
          ): F[Unit] = update(
            contestId,
            contest =>
              contest
                .copy(participants =
                  contest.participants.filter(_ != participantId)
                )
          )
        }
    }
}
