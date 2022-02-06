package me.alstepan.users

import cats.data.{EitherT, NonEmptyList}
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import me.alstepan.users.config.{AppConfig, DatabaseCredentials}
import me.alstepan.users.infra.db.{Database, UserDAO}
import me.alstepan.users.infra.endpoints.{HealthEndpoint, UserEndpoint}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.Metrics
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.implicits._
import com.comcast.ip4s._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig._
import pureconfig.generic.auto._

import scala.annotation.tailrec
import scala.util.Try

object Server extends IOApp {

  case class CmdLineArgs(confPath: Option[String] = None)

  @tailrec
  def parseArgs(args: List[String], acc: CmdLineArgs): Either[String, CmdLineArgs] = args match {
    case Nil => Right(acc)
    case "--conf" :: arg :: tail => parseArgs(tail, acc.copy(confPath = Some(arg)))
    case arg :: _ => Left(arg)
  }

  def usage(arg: String): IO[Unit] = IO(
    println(s"""Unknown argument $arg.
               |Usage: java -jar users.jar --conf <path to config file>
               |""".stripMargin)
  )

  def createServer(config: AppConfig): Resource[IO, Unit] =
    for {
      dbCreds <- Resource.eval(
        IO(
          DatabaseCredentials(
            user = Option(Try(System.getenv("DBUSER")).getOrElse("docker1")).getOrElse("docker1"),
            password = Option(Try(System.getenv("DBPASSWORD")).getOrElse("docker1")).getOrElse("docker1")
          )
        )
      )
      transactor <- Database.makeTransactor[IO](config.dbConfig, dbCreds)
      logger <- Resource.eval(Slf4jLogger.create[IO])
      userRepo <- Resource.eval(IO(UserDAO[IO](transactor)))
      metricsSvc <- PrometheusExportService.build[IO]
      userService <- Resource.eval(IO(UserEndpoint[IO](userRepo)))
      healthService <- Resource.eval(IO(HealthEndpoint(logger, userRepo)))
      metrics <- Prometheus.metricsOps[IO](metricsSvc.collectorRegistry,"userservice")
      cors = CORS.policy.withAllowOriginAll
      router = cors.apply(
        Router (
          "/" -> Metrics[IO](ops = metrics,classifierF = _ => Some("user"))(userService.service),
          "/" -> metricsSvc.routes,
          "/health" -> healthService.allServices,
        ).orNotFound
      )
      _ <- EmberServerBuilder.default[IO]
        .withHostOption(Host.fromString(config.host))
        .withPort(Port.fromInt(config.port).getOrElse(port"8000"))
        .withHttpApp(router)
        .withLogger(logger)
        .build
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    EitherT
      .fromEither[IO](parseArgs(args, CmdLineArgs()))
      .leftMap(arg => usage(arg))
      .flatMap { cmdLine =>
        EitherT.fromEither[IO] {
          cmdLine
            .confPath
            .map(path => ConfigSource.file(path).load[AppConfig])
            .fold(IO(println("Empty argument --conf")).asLeft[AppConfig])(c =>
              c.bimap(e => IO(println(s"Cannot read config: $e")), r => r))
        }
      }
      .foldF(
        err => err.as(ExitCode.Error),
        config => createServer(config).use(_ => IO.never).as(ExitCode.Success)
      )

}
