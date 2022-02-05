package me.alstepan.users.domain

import cats.data.EitherT
import me.alstepan.users.domain.Errors.Errors

trait UserRepository[F[_]] {
  def create(user: User): EitherT[F, Errors, User]
  def update(user: User): EitherT[F, Errors, User]
  def delete(id: Long): EitherT[F, Errors, Unit]
  def get(id: Long): EitherT[F, Errors, User]
  def list(): F[List[User]]
  def checkAvailability(): EitherT[F, Errors, Unit]
}
