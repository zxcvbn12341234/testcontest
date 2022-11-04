package testcontest.services

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Console
import io.circe._
import io.circe.parser.decode
import io.circe.syntax._

import java.io.{FileInputStream, FileOutputStream}

// TODO: Switch to AbstractClass style, this became a mess :D
trait BaseRepository[F[_], Key, Value] {

  protected val storageRef: Ref[F, Map[Key, Value]]
  protected val repositoryName: String

  protected def key(value: Value): Key
  protected def get(id: Key)(implicit ev: Functor[F]): F[Option[Value]] =
    storageRef.get.map(storage => storage.get(id))

  protected def getAll(implicit ev: Functor[F]): F[List[Value]] =
    storageRef.get.map(storage => storage.values.toList)

  protected def put(v: Value)(implicit ev: Functor[F]): F[Value] =
    storageRef
      .update(storage => storage + (key(v) -> v))
      .map(_ => v)

  protected def delete(id: Key)(implicit ev: Functor[F]): F[Unit] =
    storageRef
      .update(storage => storage - id)

  protected def update(id: Key, f: Value => Value)(implicit ev: Functor[F]): F[Unit] =
    storageRef
      .update(storage =>
        storage.get(id) match {
          case Some(oldValue) =>
            val newValue = f(oldValue)
            storage + (id -> newValue)
          case None => storage
      })

  // Why does this throw the error?
  // Shouldn't it be wrapped inside F?
  private def inputStream(implicit ev: Sync[F]): Resource[F, FileInputStream] =
    Resource.fromAutoCloseable(Sync[F].blocking(new FileInputStream(s"$repositoryName.json")))
  private def outputStream(implicit ev: Sync[F]): Resource[F, FileOutputStream] =
    Resource.fromAutoCloseable(Sync[F].blocking(new FileOutputStream(s"$repositoryName.json")))

  // Save current snapshot of in memory cache to JSON file
  def saveSnapshot(implicit ev0: Sync[F], ev1: Encoder[Value]): F[Unit] = outputStream.use { out =>
    getAll.map { all =>
      all.asJson.noSpaces.getBytes
    }.flatMap(bytes => Sync[F].blocking(out.write(bytes)))
  }

  def readSnapshot(implicit ev0: Sync[F], ev1: Console[F], ev2: Decoder[Value]): F[Unit] =
    inputStream.use { input =>
      Sync[F]
        .blocking(new String(input.readAllBytes()))
        .flatMap { string =>
          decode[List[Value]](string) match {
            case Right(values) =>
              Sync[F].pure(values.map(value => (key(value), value)).toMap[Key, Value])
            case Left(error) => error.raiseError[F, Map[Key, Value]]
          }
        }
        .flatMap(storage => storageRef.getAndSet(storage))
        // TODO: Probably there is something already to drop value
        .map(_ => ())
    }.handleErrorWith(err =>
      Console[F].println(s"Reading snapshot failed with $err, initializing empty snapshot"))
}
