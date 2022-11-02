package testcontest.services

import cats.Functor
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model.Participant

import java.util.UUID

trait ParticipantRepository[F[_]] {

  def getParticipant(id: UUID): F[Option[Participant]]

  def getParticipant(contestId: UUID, username: String): F[Option[Participant]]

  def putParticipant(participant: Participant): F[Participant]

  def deleteParticipant(id: UUID): F[Unit]

  def getAllParticipants(): F[List[Participant]]

}

object ParticipantRepository {

  // TODO: Rewrite with BaseRepository
  def makeInMemory[F[_]: Functor: Sync]: F[ParticipantRepository[F]] =
    Ref.of[F, Map[UUID, Participant]](Map.empty[UUID, Participant]).map {
      participantDatabaseF =>
        new ParticipantRepository[F] {
          override def getParticipant(id: UUID): F[Option[Participant]] = for {
            participantDatabase <- participantDatabaseF.get
          } yield participantDatabase.get(id)

          override def getParticipant(
              contestId: UUID,
              username: String
          ): F[Option[Participant]] = for {
            participantDatabase <- participantDatabaseF.get
          } yield participantDatabase.values.find(participant =>
            participant.contestId == contestId && participant.username == username
          )

          override def putParticipant(
              participant: Participant
          ): F[Participant] = participantDatabaseF
            .update { participantDatabase =>
              participantDatabase + (participant.participantId -> participant)
            }
            .map(_ => participant)

          override def deleteParticipant(id: UUID): F[Unit] =
            participantDatabaseF
              .update { participantDatabase =>
                participantDatabase - id
              }

          override def getAllParticipants(): F[List[Participant]] =
            participantDatabaseF.get
              .map(participantDatabase => participantDatabase.values.toList)
        }

    }
}
