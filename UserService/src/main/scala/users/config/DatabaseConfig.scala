package me.alstepan.users.config

case class DatabaseConfig(url: String, driver: String, poolSize: Int)
case class DatabaseCredentials(user: String = "docker",  password: String = "docker")
