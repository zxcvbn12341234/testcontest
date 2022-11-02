package testcontest.services

import cats.{Applicative, _}
import cats.syntax.all._
import cats.effect.Ref

import java.util.UUID

trait BaseRepository[F[_], Value] {
  protected val storageRef: Ref[F, Map[UUID, Value]]
  protected implicit val ev: Applicative[F]

  protected def get(id: UUID): F[Option[Value]] =
    storageRef.get.map(storage => storage.get(id))

  protected def getAll: F[List[Value]] =
    storageRef.get.map(storage => storage.values.toList)

  protected def put(id: UUID, v: Value): F[Value] =
    storageRef
      .update(storage => storage + (id -> v))
      .map(_ => v)

  protected def delete(id: UUID): F[Unit] =
    storageRef
      .update(storage => storage - id)

  protected def update(id: UUID, f: Value => Value): F[Unit] =
    storageRef
      .update(storage =>
        storage.get(id) match {
          case Some(oldValue) =>
            val newValue = f(oldValue)
            storage + (id -> newValue)
          case None => storage
        }
      )
}
