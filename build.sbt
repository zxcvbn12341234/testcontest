ThisBuild / organization := "com.testcontest"
ThisBuild / scalaVersion := "2.13.9"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps"
)

val dependencies = Dependencies.cats ++ Dependencies.http4s ++ Dependencies.slf4j ++ Dependencies.config ++ Dependencies.circe

lazy val root = (project in file(".")).settings(
  name := "testcontest",
  libraryDependencies ++= dependencies
)
