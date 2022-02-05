package me.alstepan.users.domain

case class User(
               id: Option[Long],
               userName: String,
               firstName: String,
               lastName: String,
               email: String,
               phone: String
               )

