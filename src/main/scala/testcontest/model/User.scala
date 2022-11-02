package testcontest.model

import io.circe._
import io.circe.generic.semiauto._

import java.time.OffsetDateTime

case class User(
    username: String,
    password: String,
    profilePhoto: String,
    createdAt: OffsetDateTime
)

object User {
  // TODO: Should come from config
  val DefaultProfilePhoto =
    "https://i.pinimg.com/474x/d8/e8/fd/d8e8fda285601c2bc45d55b449de9057.jpg"

  def user(
      username: String,
      password: String,
      profile_photo: String = DefaultProfilePhoto
  ): User = {
    User(username, password, profile_photo, OffsetDateTime.now())
  }

  object JsonSupport {
    implicit val UserJsonEncoder: Encoder[User] = deriveEncoder[User]
    implicit val UserJsonDecoder: Decoder[User] = deriveDecoder[User]
  }

}
