package me.alstepan.users.infra.endpoints

import cats.effect.Concurrent
import cats.implicits._
import doobie.Transactor
import me.alstepan.users.domain.UserRepository
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

class HealthEndpoint[F[_]: Concurrent](logger: Logger[F], userRepo: UserRepository[F]) {

  object dsl extends Http4sDsl[F]
  import dsl._

  def readiness: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> root / "health" =>
      for {
        _ <- logger.debug("Got health request")
        resp <- Ok("""{"status": "OK"}""")
        _ <- logger.debug("Responded as OK")
      } yield resp

  }

  def liveness: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> root / "liveness" =>
      for {
        _ <- logger.debug("Got liveness request")
        resp <- Ok("""{"status": "OK"}""")
      } yield resp
  }

  def allServices = liveness <+> readiness
}

object HealthEndpoint {
  def apply[F[_]:Concurrent](logger: Logger[F], userRepo: UserRepository[F]) = new HealthEndpoint[F](logger, userRepo)
}

