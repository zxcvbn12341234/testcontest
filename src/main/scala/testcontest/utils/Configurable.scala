package testcontest.utils

import cats.Functor
import cats.effect.Sync
import cats.implicits._
import com.typesafe.config.ConfigFactory
import Configuration._


trait Configurable {
  def config[F[_]: Sync]: F[ApplicationConfig] = Configurable.config
}

object Configurable {
  def config[F[_]: Sync] = Sync[F].pure(ConfigFactory.load()).map {cfg =>
    val httpServerCfg = httpServerConfig(cfg.getConfig("http-server"))
    val postgresCfg = postgresConfig(cfg.getConfig("postgres"))
  ApplicationConfig(httpServerCfg, postgresCfg)
  }
}
