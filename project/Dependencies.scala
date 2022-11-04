import sbt._

object Dependencies {

  object Versions {

    val CatsVersion                   = "2.8.0"
    val CatsEffectVersion             = "3.3.12"
    val CatsEffectTestingSpecsVersion = "1.4.0"
    val MunitCatsEffectVersion        = "1.0.7"
    val BetterMonadicForVersion       = "0.3.1"
    val CatsTimeVersion               = "0.5.0"

    val Http4sVersion = "0.23.14"

    val Slf4jVersion    = "1.7.5"
    val Log4catsVersion = "2.5.0"

    val CirceVersion = "0.14.2"

    val ConfigVersion = "1.4.2"

  }

  import Versions._

  val cats = Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % CatsEffectVersion,
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % CatsEffectVersion,
    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicForVersion),
    "org.typelevel" %% "cats-effect-testing-specs2" % CatsEffectTestingSpecsVersion % Test,
    "org.typelevel" %% "munit-cats-effect-3"        % MunitCatsEffectVersion % Test,
    "org.typelevel" %% "cats-core"                  % CatsVersion,
    "org.typelevel" %% "cats-time"                  % CatsTimeVersion
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-ember-server" % Http4sVersion,
    "org.http4s" %% "http4s-ember-client" % Http4sVersion,
    "org.http4s" %% "http4s-circe"        % Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  )

  val slf4j = Seq(
    "org.slf4j"     % "slf4j-api"       % Slf4jVersion,
    "org.slf4j"     % "slf4j-simple"    % Slf4jVersion,
    "org.typelevel" %% "log4cats-slf4j" % Log4catsVersion
  )

  val circe = Seq(
    "io.circe" %% "circe-core"    % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser"  % CirceVersion
  )

  val config = Seq(
    "com.typesafe" % "config" % ConfigVersion
  )

}
