ThisBuild / organization := "com.testcontest"
ThisBuild / scalaVersion := "2.13.9"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps"
)

val CatsVersion = "2.8.0"
val CatsEffectVersion = "3.3.12"
val CatsEffectTestingSpecsVersion = "1.4.0"
val MunitCatsEffectVersion = "1.0.7"

val Http4sVersion = "0.23.14"
val CirceVersion = "0.14.2"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % Http4sVersion,
  "org.http4s" %% "http4s-ember-client" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.typelevel" %% "log4cats-slf4j" % "2.5.0"
)

libraryDependencies += "com.typesafe" % "config" % "1.4.2"

lazy val root = (project in file(".")).settings(
  name := "testcontest",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % CatsEffectVersion,
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % CatsEffectVersion,
    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    "org.typelevel" %% "cats-effect-testing-specs2" % CatsEffectTestingSpecsVersion % Test,
    "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
    "org.typelevel" %% "cats-core" % CatsVersion,
    "org.typelevel" %% "cats-time" % "0.5.0"
  )
)
