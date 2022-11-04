package testcontest.services

import cats.Functor
import cats.effect.std.Console
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model.User
import testcontest.model.User.JsonSupport._

trait UserRepository[F[_]] {

  /** Get user with the username */
  def getUser(username: String): F[Option[User]]

  /** Put user to the database, modify if already exists */
  def putUser(user: User): F[User]

  /** Delete user from database if exists, otherwise do nothing */
  def deleteUser(username: String): F[Unit]

  /** Check if user already exists */
  def exists(username: String): F[Boolean]

  /** Returns all of the users stored */
  def getAllUsers: F[List[User]]

  def start: F[Unit]
  def finished: F[Unit]

}

object UserRepository {
  def makeInMemory[F[_]: Functor: Sync: Console]: F[UserRepository[F]] =
    Ref.of[F, Map[String, User]](Map.empty[String, User]).map { userDatabaseF =>
      new UserRepository[F] with BaseRepository[F, String, User] {
        override val storageRef: Ref[F, Map[String, User]] = userDatabaseF

        override val repositoryName = "user-repository"

        override def key(value: User): String = value.username

        override def getUser(username: String): F[Option[User]] = get(username)

        override def putUser(user: User): F[User] = put(user)

        override def deleteUser(username: String): F[Unit] = delete(username)

        override def exists(username: String): F[Boolean] =
          getUser(username).map(_.isDefined)

        override def getAllUsers: F[List[User]] = getAll

        override def start: F[Unit] =
          Console[F].println("Initializing user repository from file...") >>
            readSnapshot >>
            Console[F].println("Initialized.")

        override def finished: F[Unit] =
          Console[F].println("Saving user repository file...") >>
            saveSnapshot >>
            Console[F].println("Saved.")
      }

    }
}
