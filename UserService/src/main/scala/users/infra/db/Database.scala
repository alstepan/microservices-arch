package me.alstepan
package users.infra.db

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.{Escape, Literal, LowerCase, NamingStrategy}
import me.alstepan.users.config.{DatabaseConfig, DatabaseCredentials}

object Database {

  val doobieContext = new DoobieContext.Postgres(NamingStrategy(Escape, Literal, LowerCase))

  def makeTransactor[F[_]: Async](dbConf: DatabaseConfig, dbCreds: DatabaseCredentials): Resource[F, HikariTransactor[F]] =
    for {
      ec <- Resource.eval(Async[F].executionContext)
      res <- HikariTransactor.newHikariTransactor(dbConf.driver, dbConf.url, dbCreds.user, dbCreds.password, ec)
    } yield res
}
