package testcontest.services

import cats.syntax.all._
import cats.{ApplicativeError, Monad, MonadError}
import testcontest.model.User

import java.time.OffsetDateTime

trait UserService[F[_]] {

  /** Register new user */
  def register(username: String, password: String): F[User]

  /** Change profile photo of existing user */
  def changeProfilePhoto(username: String, newProfilePhoto: String): F[User]

  /** Filter users */
  def getUsers(
      username: Option[String],
      createdBefore: Option[OffsetDateTime]
  ): F[List[User]]

  /** Get all users without any filters, same as getUsers without any filter specified */
  def getAllUsers: F[List[User]] = getUsers(None, None)

}

object UserService {

  case object UserAlreadyExists extends Throwable
  case object UserCouldNotFound extends Throwable

  def make[F[_]](
      userRepository: UserRepository[F]
  )(implicit ev: MonadError[F, Throwable]): UserService[F] =
    new UserService[F] {

      private def runIfNotExists[A](username: String)(f: Unit => F[A]): F[A] =
        userRepository.exists(username).flatMap { exists =>
          if (exists) {
            UserAlreadyExists.raiseError
          } else {
            f()
          }
        }

      private def runIfExists[A](username: String)(f: User => F[A]): F[A] =
        userRepository.getUser(username).flatMap {
          case None =>
            UserCouldNotFound.raiseError
          case Some(user) => f(user)
        }

      override def register(username: String, password: String): F[User] =
        runIfNotExists(username) { _ =>
          val user = User.user(username, password)
          userRepository.putUser(user)
        }

      override def changeProfilePhoto(
          username: String,
          newProfilePhoto: String
      ): F[User] = runIfExists(username) { user =>
        val updatedUser = user.copy(profilePhoto = newProfilePhoto)
        userRepository.putUser(updatedUser)
      }

      override def getUsers(
          usernameOpt: Option[String],
          createdBeforeOpt: Option[OffsetDateTime]
      ): F[List[User]] =
        userRepository
          .getAllUsers
          .map(users =>
            users.filter { user =>
              usernameOpt match {
                case Some(un) => user.username == un
                case None     => true
              }
            }.filter { user =>
              createdBeforeOpt match {
                case Some(cb) => user.createdAt.isBefore(cb)
                case None     => true
              }
          })
    }
}
