package testcontest

import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.{Logger, Timeout}
import org.typelevel.log4cats._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import testcontest.api._
import testcontest.services.{
  ContestRepository,
  ContestService,
  ParticipantRepository,
  QuestionRepository,
  QuestionService,
  UserRepository,
  UserService
}
import testcontest.utils.Configurable

import scala.concurrent.duration._

object Main extends IOApp.Simple with Configurable {

  // TODO: Check other ways of creating logger
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] =
    for {
      userRepository <- UserRepository.makeInMemory[IO]
      _              <- userRepository.start

      questionRepository <- QuestionRepository.makeInMemory[IO]
      _                  <- questionRepository.start

      participantRepository <- ParticipantRepository.makeInMemory[IO]
      _                     <- participantRepository.start

      contestRepository <- ContestRepository.makeInMemory[IO]
      _                 <- contestRepository.start

      userService     = UserService.make[IO](userRepository)
      questionService = QuestionService.make[IO](questionRepository)
      contestService = ContestService
        .make[IO](contestRepository, participantRepository, questionRepository)

      userRoutes: HttpRoutes[IO] = new UserRoutes[IO](userService).allRoutes

      questionRoutes: HttpRoutes[IO] = new QuestionRoutes[IO](
        questionService
      ).allRoutes

      contestRoutes: HttpRoutes[IO] = new ContestRoutes[IO](
        contestService
      ).allRoutes

      adminRoutes: HttpRoutes[IO] = new AdminRoutes(
        contestRepository,
        userRepository,
        participantRepository,
        questionRepository
      ).allRoutes

      allRoutes = userRoutes <+> questionRoutes <+> contestRoutes <+> adminRoutes

      middleWares = { http: HttpRoutes[IO] =>
        Timeout(60.seconds)(http)
      }

      apis = Logger.httpApp(true, true)(
        middleWares(
          Router(
            "/api" -> allRoutes
          )
        ).orNotFound
      )
      cfg <- config[IO]
      httpCfg = cfg.httpConfig
      // what mess is this
      exitCode <- (for {
        _ <- Stream
          .resource[IO, Unit](
            EmberServerBuilder
              .default[IO]
              .withHost(
                Ipv4Address.fromString(httpCfg.host).get
              )
              .withPort(Port.fromInt(httpCfg.port).get)
              .withHttpApp(apis)
              .build
              .flatMap(_ => Resource.eval(Async[IO].never))
          )
          // Save snapshots just before server is closed
          .onFinalize(
            userRepository.finished >> participantRepository.finished >> questionRepository.finished >> contestRepository.finished)
        _ <- Stream.eval(IO.println(s"Server started with config $httpCfg"))
      } yield ()).compile.drain.as(ExitCode.Success)

    } yield exitCode
}
