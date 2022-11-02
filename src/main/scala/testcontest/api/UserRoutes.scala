package testcontest.api

import cats.Monad
import cats.effect.{Async, Concurrent}
import cats.syntax.all._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import testcontest.model.User.JsonSupport._
import testcontest.services.UserService
import testcontest.services.UserService.UserAlreadyExists

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

final class UserRoutes[F[_]: JsonDecoder: Monad: Async: Concurrent](
    userService: UserService[F]
) {

  import UserRoutes._

  def userRoutes: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "user" :? OptionalUsernameQueryParamMatcher(
            username
          ) +& OptionalCreatedBeforeQueryParamMatcher(createdBefore) =>
        userService
          .getUsers(username, createdBefore)
          .flatMap(users => Ok(users.asJson))
      case req @ (POST -> Root / "user") =>
        req
          .decodeJson[UserCreateRequest]
          .flatMap(userCreateRequest =>
            userService
              .register(userCreateRequest.username, userCreateRequest.password)
          )
          .flatMap(user => Ok(user.asJson))
          .handleErrorWith {
            case InvalidMessageBodyFailure(details, _) =>
              BadRequest(s"Failed to deserialize user\n$details")
            case UserAlreadyExists => BadRequest("User already exists")
          }
    }
  }

  def allRoutes: HttpRoutes[F] = {
    userRoutes
  }

}

object UserRoutes {
  implicit val offsetDateTimeDecoder: QueryParamDecoder[OffsetDateTime] =
    QueryParamDecoder.offsetDateTime(DateTimeFormatter.ISO_DATE_TIME)

  object OptionalUsernameQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("username")
  object OptionalCreatedBeforeQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[OffsetDateTime]("createdBefore")

  case class UserCreateRequest(username: String, password: String)
  implicit val UserCreateRequestEncoder: Decoder[UserCreateRequest] =
    deriveDecoder[UserCreateRequest]
}
