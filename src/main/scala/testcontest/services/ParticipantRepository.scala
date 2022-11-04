package testcontest.services

import cats.effect.std.Console
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model.Participant
import testcontest.model.Participant.JsonSupport._

import java.util.UUID

trait ParticipantRepository[F[_]] {

  def getParticipant(id: UUID): F[Option[Participant]]

  def getParticipant(contestId: UUID, username: String): F[Option[Participant]]

  def putParticipant(participant: Participant): F[Participant]

  def deleteParticipant(id: UUID): F[Unit]

  def getAllParticipants: F[List[Participant]]

  def start: F[Unit]
  def finished: F[Unit]

}

object ParticipantRepository {

  def makeInMemory[F[_]: Sync: Console]: F[ParticipantRepository[F]] =
    Ref.of[F, Map[UUID, Participant]](Map.empty[UUID, Participant]).map { participantDatabaseF =>
      new ParticipantRepository[F] with BaseRepository[F, UUID, Participant] {

        protected val storageRef: Ref[F, Map[UUID, Participant]] = participantDatabaseF

        override val repositoryName = "participant-repository"

        override def key(value: Participant): UUID = value.id

        override def getParticipant(id: UUID): F[Option[Participant]] = get(id)

        override def getParticipant(
            contestId: UUID,
            username: String
        ): F[Option[Participant]] =
          getAll.map(_.find(participant =>
            participant.contestId == contestId && participant.username == username))

        override def putParticipant(
            participant: Participant
        ): F[Participant] = put(participant)

        override def deleteParticipant(id: UUID): F[Unit] = delete(id)

        override def getAllParticipants: F[List[Participant]] = getAll

        override def start: F[Unit] =
          Console[F].println("Initializing participant repository from file...") >>
            readSnapshot >>
            Console[F].println("Initialized.")

        override def finished: F[Unit] =
          Console[F].println("Saving participant repository file...") >>
            saveSnapshot >>
            Console[F].println("Saved.")

      }

    }
}
