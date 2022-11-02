package testcontest.utils

import com.typesafe.config.Config

object Configuration {

  case class ApplicationConfig(
      httpConfig: HttpServerConfig,
      postgresConfig: PostgresConfig
  )

  case class HttpServerConfig(host: String, port: Int)
  case class PostgresConfig(url: String)

  private[utils] def httpServerConfig(cfg: Config): HttpServerConfig =
    HttpServerConfig(cfg.getString("host"), cfg.getInt("port"))

  private[utils] def postgresConfig(cfg: Config): PostgresConfig =
    PostgresConfig(cfg.getString("url"))

}
