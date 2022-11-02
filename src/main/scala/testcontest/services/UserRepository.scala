package testcontest.services

import cats.effect.{Ref, Sync}
import cats.{Functor, Monad, MonadThrow}
import cats.syntax.all._
import testcontest.model.User

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
  def getAllUsers(): F[List[User]]

}

object UserRepository {

  // TODO: Rewrite with BaseRepository
  def makeInMemory[F[_]: Functor: Sync]: F[UserRepository[F]] =
    Ref.of[F, Map[String, User]](Map.empty[String, User]).map { userDatabaseF =>
      new UserRepository[F] {
        override def getUser(username: String): F[Option[User]] = for {
          userDatabase <- userDatabaseF.get
        } yield userDatabase.get(username)

        override def putUser(user: User): F[User] = userDatabaseF
          .update { userDatabase =>
            userDatabase + (user.username -> user)
          }
          .map(_ => user)

        override def deleteUser(username: String): F[Unit] = userDatabaseF
          .update { userDatabase =>
            userDatabase - username
          }

        override def exists(username: String): F[Boolean] =
          getUser(username).map(_.isDefined)

        override def getAllUsers(): F[List[User]] =
          userDatabaseF.get.map(userDatabase => userDatabase.values.toList)
      }

    }
}
