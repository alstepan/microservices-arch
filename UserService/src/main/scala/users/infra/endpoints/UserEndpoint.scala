package me.alstepan.users.infra.endpoints

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits._
import cats.effect.implicits._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.parser.parse
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import me.alstepan.users.domain.{Errors, User, UserRepository}
import me.alstepan.users.domain.Errors._

class UserEndpoint[F[_]: Concurrent](userRepo: UserRepository[F]) {

  object dsl extends Http4sDsl[F]
  import dsl._

  implicit val decodeOffice: Decoder[User] = deriveDecoder[User]
  implicit val encodeOffice: Encoder[User] = deriveEncoder[User]

  def createUser: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> root / "user" =>
      for {
        user <- req.decodeJson[User]
        response <- result(userRepo.create(user))
      } yield response
  }

  def getUser: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> root / "user" / LongVar(userId) =>
      result(userRepo.get(userId))
  }

  def deleteUser: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> root / "user" / LongVar(userId) =>
      result(userRepo.delete(userId))
  }

  def updateUser: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> root / "user" / LongVar(userId) =>
      for {
        userBody <- req.bodyText.compile.string.flatMap(s => parse(s).flatMap(_.as[User]).pure[F])
        response <- userBody.fold (
          err => BadRequest(CustomError("Cannot parse request body").toJson),
          user => result(userRepo.update(user.copy(id = Some(userId))))
        )
      } yield response
  }

  def service = createUser <+> getUser <+> deleteUser <+> updateUser

  def result[T](result: EitherT[F, Errors.Errors, T]): F[Response[F]] =
    result.foldF(
      {
        case NotFoundError => NotFound(NotFoundError.toJson)
        case UniqueConstraintError => BadRequest(UniqueConstraintError.toJson)
        case c: CustomError => InternalServerError(c.toJson)
        case e => InternalServerError(CustomError("Something went wrong").toJson)
      },
      {
        case u: User => Ok(u.asInstanceOf[User].asJson)
        case empty: Unit => NoContent()
      }
    )
}

object UserEndpoint {
  def apply[F[_]: Concurrent](userRepo: UserRepository[F]) = new UserEndpoint[F](userRepo)
}
