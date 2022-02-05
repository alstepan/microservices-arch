package me.alstepan.users.infra.db

import cats.syntax.either._
import cats.implicits._
import cats.effect.implicits._
import cats.data.EitherT
import cats.effect.MonadCancelThrow
import doobie.Transactor
import doobie.quill.DoobieContext
import doobie.implicits._
import io.getquill.{CompositeNamingStrategy3, EntityQuery, Escape, Literal, LowerCase}
import me.alstepan.users.domain.Errors.{CustomError, Errors, NotFoundError}
import me.alstepan.users.domain.{Errors, User, UserRepository}

class UserDAO[F[_]: MonadCancelThrow](tr: Transactor[F]) extends UserRepository[F] {

  val dc: DoobieContext.Postgres[CompositeNamingStrategy3[Escape.type, Literal.type, LowerCase.type]] = Database.doobieContext

  import dc._

  val userSchema: dc.Quoted[EntityQuery[User]] = quote {
    querySchema[User](""""users"""")
  }

  override def create(user: User): EitherT[F, Errors.Errors, User] =
    EitherT {
      dc.run(userSchema.insert(lift(user)).returningGenerated(_.id))
        .transact(tr)
        .map {
          case Some(i) => Right(user.copy(id = Some(i)))
          case _ => Left(CustomError(s"Cannot insert user $user"))
        }
    }

  override def update(user: User): EitherT[F, Errors.Errors, User] =
    EitherT {
      dc.run(userSchema.filter(u => u.id == lift(user.id)).update(
        _.firstName -> lift(user.firstName), 
        _.lastName -> lift(user.lastName), 
        _.phone -> lift(user.phone), 
        _.email -> lift(user.email),
        _.userName -> lift(user.userName)))
        .transact(tr)
        .map(rows => if (rows === 1) Right(user) else Left(NotFoundError))
    }

  override def delete(id: Long): EitherT[F, Errors.Errors, Unit] =
    EitherT {
      dc.run(userSchema.filter(u => u.id == lift(Option(id))).delete)
        .transact(tr)
        .map { rows =>
          if (rows == 1) Right(()) else Left(NotFoundError)
        }
    }

  override def get(id: Long): EitherT[F, Errors.Errors, User] =
    EitherT {
      dc.run(userSchema.filter(u => u.id == lift(Option(id))))
        .map(r => r.headOption.toRight[Errors.Errors](NotFoundError))
        .transact(tr)
    }

  override def list(): F[List[User]] =
    dc.run(userSchema).map(x => x).transact(tr)

  override def checkAvailability(): EitherT[F, Errors, Unit] =
    EitherT {
      dc.run(userSchema.take(1))
        .map(_.headOption.toRight[Errors.Errors](Errors.CustomError("Connection error")))
        .map(res => res.map(_ => ()))
        .transact(tr)
    }

}

object UserDAO {
  def apply[F[_]: MonadCancelThrow](tr: Transactor[F]) = new UserDAO[F](tr)
}
